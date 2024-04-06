package org.amber.asparagus.fkec

import org.amber.asparagus.fkec.crypto.CryptoUtils
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServerWithInterceptorApplication

fun main(args: Array<String>) {
	CryptoUtils.registerBouncyCastleProvider()
	runApplication<ServerWithInterceptorApplication>(*args)
}
