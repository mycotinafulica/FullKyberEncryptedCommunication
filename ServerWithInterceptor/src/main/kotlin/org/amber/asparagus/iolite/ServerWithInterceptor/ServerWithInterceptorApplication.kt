package org.amber.asparagus.iolite.ServerWithInterceptor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServerWithInterceptorApplication

fun main(args: Array<String>) {
	runApplication<ServerWithInterceptorApplication>(*args)
}
