package org.amber.asparagus.fkec.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse

class KyberFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse
                          , chain: FilterChain) {

        println("My filter is called")

        chain.doFilter(request, response)
    }
}