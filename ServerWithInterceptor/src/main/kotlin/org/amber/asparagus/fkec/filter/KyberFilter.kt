package org.amber.asparagus.fkec.filter

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

/*
* Currently only GET & POST requests are supported. Only json body is currently supported, raw data for images, videos,
* etc., is not yet supported.
*
* On post request, the following will be encrypted into the body:
* The body itself, the query parameters and the custom headers (headers with x-*)
*
* The layout of the payload of encryption will be as the following :
* ~~~BODY~~~<body>~~~PARAMS~~~<original_param_value>~~~HEADERS~~~<header1:header_value>||<header2:header_value>
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
* The session info will always be sent as a header in x-session-info, this one will not be encrypted (as it already
* encrypted when returned from the server anyway.
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

class KyberFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse
                          , chain: FilterChain) {

        println("===========================FILTER===========================")
        val httpRequest = request as HttpServletRequest

        if(httpRequest.method != "POST" && httpRequest.method != "GET") {
            chain.doFilter(request, response)
            return
        }

        // TODO : Do checks to support only json body.

        println("Http method : ${httpRequest.method}")
        println("Url : ${httpRequest.requestURL}")
        println("Query : ${httpRequest.queryString}")

        println("Parameters : ")
        httpRequest.parameterMap.forEach { (k, v) ->
            println("$k : ${v.contentToString()}")
        }

        println("Headers : ")
        httpRequest.headerNames.asIterator().forEach {
            val headerVal = httpRequest.getHeader(it)
            println("$it : $headerVal")
        }


        if(httpRequest.method == "POST") {
            println("Body : ")
            val requestWrapper = RequestWrapper(httpRequest)
            val body = requestWrapper.inputStream.bufferedReader().use { it.readText() }
            println(body)

            chain.doFilter(requestWrapper, response)
        }
        else {
            chain.doFilter(httpRequest, response)
        }

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

class RequestWrapper(request: HttpServletRequest)
    : HttpServletRequestWrapper(request) {

    private val body: String = request.inputStream.bufferedReader().use {
        it.readText()
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