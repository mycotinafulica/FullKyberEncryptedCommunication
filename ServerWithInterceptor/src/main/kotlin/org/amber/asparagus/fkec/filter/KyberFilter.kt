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
* ~~~BODY~~~<body>~~~PARAMS~~~<param1:param_value>,<param2:param_value>~~~HEADERS~~~<header1:header_value>
*
* The encrypted data will be put raw on the body, decrypted & reconstructed on the spring-boot filter before going
* into the controller service.
*
* For get request, since it is not recommended to put body there, then the encrypted data will be put as query params
* with the following layout :
* ~~~PARAMS~~~<param1:param_value>,<param2:param_value>~~~HEADERS~~~<header1:header_value>
*
* and will be put as the query parameter :
* https://domain/endpoint?data=encrypted_data
*
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