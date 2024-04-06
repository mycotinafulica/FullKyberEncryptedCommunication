package org.amber.asparagus.fkec.filter

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

class KyberFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse
                          , chain: FilterChain) {

        println("===========================FILTER===========================")
        val httpRequest = request as HttpServletRequest

        println("Parameters : ")
        httpRequest.parameterMap.forEach { (k, v) ->
            println("$k : $v")
        }

        println("Headers : ")
        httpRequest.headerNames.asIterator().forEach {
            val headerVal = httpRequest.getHeader(it)
            println("$it : $headerVal")
        }

        println("Body : ")
        val requestWrapper = RequestWrapper(httpRequest)
        val body = requestWrapper.inputStream.bufferedReader().use { it.readText() }
        println(body)

        chain.doFilter(requestWrapper, response)

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