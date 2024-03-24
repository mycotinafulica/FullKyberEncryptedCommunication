package org.amber.asparagus.fkec

import org.amber.asparagus.fkec.crypto.Utils
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServerWithInterceptorApplication

fun main(args: Array<String>) {
	Utils.registerBouncyCastleProvider()
	runApplication<ServerWithInterceptorApplication>(*args)
}
