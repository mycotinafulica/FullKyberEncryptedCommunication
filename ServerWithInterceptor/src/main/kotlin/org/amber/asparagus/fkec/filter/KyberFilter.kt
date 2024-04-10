package org.amber.asparagus.fkec.filter

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import org.amber.asparagus.fkec.crypto.CryptoUtils
import org.amber.asparagus.fkec.crypto.SessionDb
import org.amber.asparagus.fkec.filter.KyberFilterConstant.Companion.METHOD_GET
import org.amber.asparagus.fkec.filter.KyberFilterConstant.Companion.METHOD_POST
import org.amber.asparagus.fkec.filter.KyberFilterConstant.Companion.ORIG_CONTENT_TYPE_HEADER
import org.amber.asparagus.fkec.filter.KyberFilterConstant.Companion.SESSION_INFO_HEADER
import org.amber.asparagus.fkec.util.BaosWrapper
import org.springframework.http.MediaType
import java.io.*
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
* For response, though, since parameter is of not importance, the format will only be
* ~~~BODY~~~<body>~~~HEADERS~~~<header1:header_value>||<header2:header_value>
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

// TODO : Question -> what will happen if the request / response body is so so so so so so large
// This filter will be in effect for /post-handshake/* endpoints
class KyberFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse
                          , chain: FilterChain) {

        println("===========================FILTER===========================")
        val httpRequest = request as HttpServletRequest

        if(httpRequest.method != METHOD_POST
            && httpRequest.method != METHOD_GET) {
            chain.doFilter(request, response)
            return
        }

        val wrappedRequest = RequestWrapper(httpRequest)
        val capturingResp  = ResponseWrapper(response as HttpServletResponse)

        chain.doFilter(wrappedRequest, capturingResp)

//        println("Filter chain done, now on response part, is commited : ${response.isCommitted}}")

        val respBody            = capturingResp.getCapturedBody()
        val modifiedBodyBuilder = StringBuilder()

        modifiedBodyBuilder.append("~~~BODY~~~$respBody~~~HEADERS~~~")
        var headersToEnc = ""
        capturingResp.filteredHeader.forEach {
            it.value.forEach { value ->
                headersToEnc += "${it.key}:$value||"
            }
        }

        if(headersToEnc.endsWith("||")) {
            modifiedBodyBuilder.append(headersToEnc.substring(0, headersToEnc.length - 2))
        }
        else {
            modifiedBodyBuilder.append(headersToEnc)
        }

        val sessionId = httpRequest.getHeader(SESSION_INFO_HEADER)
        val encryptedModifiedResp = CryptoUtils.aesGcmEncrypt(modifiedBodyBuilder.toString()
            , SessionDb.sessions[sessionId]!!)

        response.setContentLength(encryptedModifiedResp.length)
        response.contentType = MediaType.TEXT_PLAIN_VALUE
//        println("Writing to response")
        response.writer.write(encryptedModifiedResp)

        // TODO : After the response & headers are captured, we need to do the following :
        // TODO : 1. Modify the response & add the headers and encrypt them
        // TODO : 2. Write the modified response to the actual response
        // Notable observation : it seems the buffer will not be committed until all filters are executed.
        // Still need to test  : A large response body that exceed 8192 bytes, how it will be handled? It seems that
        // chunked encoding will apply, but how will it affect the servlet filter?
        // It might be the case that after encrypted the new length will exceed the buffer size for the chunk
        // In which case, we need to set the buffer size of the response (since we intercept the response by capturing it,
        // there's shouldn't be any bytes written to it yet, and thus the buffer size can still be modified.

        println("===========================FILTER END===========================")
    }
}

private class ResponseWrapper(response: HttpServletResponse)
    : HttpServletResponseWrapper(response) {

    private var baosWrapper = BaosWrapper(ByteArrayOutputStream(response.bufferSize))
    val filteredHeader = HashMap<String, MutableList<String>>()
    var getOutputStreamCalled = false
        private set
    var getWriterCalled = false
        private set

    fun getCapturedBody(): String {
        /* TODO : We might need to consider Base64 to support binary output, or better yet, instead of operating on
        *  TODO string level, it could be better to operate on byte array to reduce size. It can be future improvement.
        */
        return String(baosWrapper.toByteArray(), Charsets.UTF_8)
    }

    // We will intercept writing here so the writing will not go to the response's output stream just yet
    override fun getOutputStream(): ServletOutputStream {
        if(getWriterCalled) throw IllegalStateException("getWriter already called prior calling getOutputStream")

        getOutputStreamCalled = true
        return baosWrapper.baosServlet
    }

    override fun getWriter(): PrintWriter {
        if(getOutputStreamCalled) throw IllegalStateException("getOutputStream already called prior calling getWriter")

        getWriterCalled = true

        return baosWrapper.printWriter
    }

    override fun addHeader(name: String, value: String) {
        // Filer x-* header for being written, as it should be encrypted
        if(name.lowercase().startsWith("x-")
            && name.lowercase() != "x-orig-content-type") {
            if(filteredHeader.contains(name)) {
                val currList = filteredHeader[name]!!
                currList.add(value)
                filteredHeader[name] = currList
            }
            else {
                filteredHeader[name] = arrayListOf(value)
            }
            return
        }
        super.addHeader(name, value)
    }

    override fun addIntHeader(name: String, value: Int) {
        addHeader(name, "" + value)
    }

    override fun setBufferSize(size: Int) {
        super.setBufferSize(size)
        baosWrapper.setBufferSize(size)
    }

    override fun resetBuffer() {
        super.resetBuffer()
        baosWrapper.reset()
    }

    override fun flushBuffer() {
        // no-op, we don't want to commit the response yet
//        println("Buffer is flushed")
    }

    override fun reset() {
        super.reset()
        baosWrapper.reset()
    }
}

private class RequestWrapper(private val request: HttpServletRequest)
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