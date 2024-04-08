package org.amber.asparagus.fkec.filter

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.amber.asparagus.fkec.crypto.CryptoUtils
import org.amber.asparagus.fkec.crypto.SessionDb
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import org.amber.asparagus.fkec.filter.KyberFilterConstant.Companion.METHOD_GET
import org.amber.asparagus.fkec.filter.KyberFilterConstant.Companion.METHOD_POST
import org.amber.asparagus.fkec.filter.KyberFilterConstant.Companion.ORIG_CONTENT_TYPE_HEADER
import org.amber.asparagus.fkec.filter.KyberFilterConstant.Companion.SESSION_INFO_HEADER
import java.util.*
import kotlin.collections.HashMap

/*
* Currently only GET & POST requests are supported. Only json body is currently supported, raw data for images, videos,
* etc., is not yet supported.
*
* On post request, the following will be encrypted into the body:
* The body itself, the query parameters and the custom headers (headers with x-*)
*
* The layout of the payload of encryption will be as the following :
* ~~~BODY~~~<body>~~~PARAMS~~~<original_param_value>~~~HEADERS~~~<header1:header_value>||<header2:header_value>
* If the header has the same header key, it will still be appended as separate header (instead of comma separated value)
*
* The encrypted data will be put raw on the body, decrypted & reconstructed on the spring-boot filter before going
* into the controller service.
*
* For get request, since it is not recommended to put body there, then the encrypted data will be put as query params
* with the following layout :
* ~~~PARAMS~~~<original_param_value>~~~HEADERS~~~<header1:header_value>
*
* and will be put as the query parameter :
* https://domain/endpoint?data=encrypted_data
*
* The session info will always be sent as a header in x-session-info, this one will not be encrypted, and not need to
* be kept secret.
*
* Since the body will be formatted into a raw encrypted data, there will also be one additional header
* which is x-orig-content-type, which will store the original content type value (only for POST method)
*
* */

/*
* Temp comment
* Http method : POST
Url : http://localhost:8080/fkec/post-handshake/transaction
Query : param1=pararam&param2=2500
Parameters :
param1 : [pararam]
param2 : [2500]
Headers :
x-header1 : MyHeader1
x-header2 : MyHeader2
x-session-info : PpTOp1OR5//B6bdT8m9O986v73enDXdWXW9prEEg2JAdLjmIRYw8SeoTT9P4iS8xFgxK/Zidozr26RQ9gN4plg==
content-type : application/json; charset=utf-8
content-length : 45
host : localhost:8080
connection : Keep-Alive
accept-encoding : gzip
user-agent : okhttp/4.12.0
Body :
{"field1":"myField","field2":0,"field3":true}
* */


// This filter will be in effect for /post-handshake/* endpoints
class KyberFilter : Filter {
    companion object {

    }
    override fun doFilter(request: ServletRequest, response: ServletResponse
                          , chain: FilterChain) {

        println("===========================FILTER===========================")
        val httpRequest = request as HttpServletRequest

        if(httpRequest.method != METHOD_POST
            && httpRequest.method != METHOD_GET) {
            chain.doFilter(request, response)
            return
        }

        // TODO : Do checks to support only json body.

        val wrappedRequest = RequestWrapper(httpRequest)
        chain.doFilter(wrappedRequest, response)

//        println("Http method : ${httpRequest.method}")
//        println("Url : ${httpRequest.requestURL}")
//        println("Query : ${httpRequest.queryString}")
//
//        println("Parameters : ")
//        httpRequest.parameterMap.forEach { (k, v) ->
//            println("$k : ${v.contentToString()}")
//        }
//
//        println("Headers : ")
//        httpRequest.headerNames.asIterator().forEach {
//            val headerVal = httpRequest.getHeader(it)
//            println("$it : $headerVal")
//        }
//
//
//        if(httpRequest.method == "POST") {
//            println("Body : ")
//            val requestWrapper = RequestWrapper(httpRequest)
//            val body = requestWrapper.inputStream.bufferedReader().use { it.readText() }
//            println(body)
//
//            chain.doFilter(requestWrapper, response)
//        }
//        else {
//            chain.doFilter(httpRequest, response)
//        }

        println("Responses : ")
        val httpResponse = response as HttpServletResponse
        println("Headers : ")
        httpResponse.headerNames.forEach {
            val headerVal = httpResponse.getHeader(it)
            println("$it : $headerVal")
        }

        println("===========================FILTER END===========================")
    }
}

class RequestWrapper(private val request: HttpServletRequest)
    : HttpServletRequestWrapper(request) {

    private var body: String        = ""
    private var queryParams: String = ""
    private val headerMap           = HashMap<String, MutableList<String>>()
    private val parameterMap        = HashMap<String, MutableList<String>>()

    init {
        val sessionId = request.getHeader(SESSION_INFO_HEADER)
        val encryptedPayload: String

        if(request.method == METHOD_POST) {
            encryptedPayload = request.inputStream.bufferedReader().use {
                it.readText()
            }
        }
        else { // if method is get
            val splitParam = request.queryString.split("=")
            encryptedPayload = splitParam[1]
        }

        // Even if the request have no body, it will still have body here since at least
        // the ~~~BODY~~~~~~PARAMS~~~~~~HEADERS~~~ is present
        val key = SessionDb.sessions[sessionId]
        val decryptedBody = CryptoUtils.aesGcmDecrypt(encryptedPayload, key!!)

        val headerSplit = decryptedBody.split("~~~HEADERS~~~")
        if(headerSplit.size > 1) {
            reconstructHeaders(headerSplit[1])
        }
        val paramSplit = headerSplit[0].split("~~~PARAMS~~~")
        if(paramSplit.size > 1) {
            queryParams = paramSplit[1]
            val paramPairs = queryParams.split("&")
            paramPairs.forEach {
                val valuePair = it.split("=")
                addParameter(valuePair[0], valuePair[1])
            }
        }

        if(request.method == METHOD_POST) {
            val bodySplit = paramSplit[0].split("~~~BODY~~~")
            if(bodySplit.size > 1) {
                body = bodySplit[1]
            }
        }
    }

    private fun reconstructHeaders(headerPart: String) { // headerPart = decryptedHeaderPart
        // adding headers from the request
        request.headerNames.asIterator().forEach {
            val values = request.getHeaders(it)
            values.asIterator().forEach { value ->
                addHeader(it, value)
            }
        }
        // adding the decrypted part :
        val decryptedHeaderPairs = headerPart.split("||")
        decryptedHeaderPairs.forEach {
            val splitPair = it.split(":")
            addHeader(splitPair[0], splitPair[1])
        }

        if(headerMap[ORIG_CONTENT_TYPE_HEADER] != null) {
            headerMap["content-type"] = arrayListOf(headerMap[ORIG_CONTENT_TYPE_HEADER]!![0])
        }
    }

    private fun addParameter(key: String, value: String) {
        if(parameterMap.contains(key)) {
            val currArray = parameterMap[key]!!
            currArray.add(value)
            parameterMap[key] = currArray
        }
        else {
            parameterMap[key] = arrayListOf(value)
        }
    }

    private fun addHeader(key: String, value: String) {
        if(headerMap.contains(key)) {
            val currArray = headerMap[key]!!
            currArray.add(value)
            headerMap[key] = currArray
        }
        else {
            headerMap[key] = arrayListOf(value)
        }
    }

    override fun getHeaderNames(): Enumeration<String> {
        return Collections.enumeration(headerMap.keys)
    }

    override fun getHeader(name: String): String {
        val normalizedName = name.lowercase(Locale.getDefault())
        return if(headerMap[normalizedName] != null) headerMap[normalizedName]!![0] else ""
    }

    override fun getHeaders(name: String): Enumeration<String>? {
       val normalizedName = name.lowercase(Locale.getDefault())
       return if(headerMap[normalizedName] != null) {
           val values = headerMap[normalizedName]!!
           Collections.enumeration(values)
       }
        else {
            null
       }
    }

    override fun getQueryString(): String {
        return queryParams
    }

    override fun getParameterMap(): MutableMap<String, Array<String>> {
        val copy = HashMap<String, Array<String>>()
        parameterMap.forEach { (k, v) ->
            copy[k] = v.toTypedArray()
        }

        return copy
    }

    override fun getParameterNames(): Enumeration<String> {
        return Collections.enumeration(parameterMap.keys)
    }

    override fun getParameter(name: String): String? {
        return if(parameterMap.contains(name)) {
            parameterMap[name]!![0]
        }
        else {
            null
        }
    }

    override fun getParameterValues(name: String): Array<String>? {
        return if(parameterMap.contains(name)) {
            parameterMap[name]!!.toTypedArray()
        }
        else {
            null
        }
    }

    override fun getInputStream(): ServletInputStream {
        val byteInputStream = ByteArrayInputStream(body.toByteArray())

        val servletInputStream = object : ServletInputStream() {
            override fun read(): Int {
                return byteInputStream.read()
            }

            override fun isFinished(): Boolean {
                return byteInputStream.available() == 0
            }

            override fun isReady(): Boolean {
                return true
            }

            override fun setReadListener(p0: ReadListener?) {
            }
        }


        return servletInputStream
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(inputStream))
    }
}

private class KyberFilterConstant {
    companion object {
        const val METHOD_GET  = "GET"
        const val METHOD_POST = "POST"
        const val SESSION_INFO_HEADER = "x-session-info"
        const val ORIG_CONTENT_TYPE_HEADER = "x-orig-content-type"
    }
}