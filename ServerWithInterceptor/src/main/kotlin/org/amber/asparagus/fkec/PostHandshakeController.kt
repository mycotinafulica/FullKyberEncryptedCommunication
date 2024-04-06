package org.amber.asparagus.fkec

import org.amber.asparagus.fkec.dto.TransactionRequest
import org.amber.asparagus.fkec.dto.TransactionResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/fkec/post-handshake")
class PostHandshakeController {
    @GetMapping("/test")
    fun test(): String {
        return "Hello world"
    }

    @GetMapping("/test/test2")
    fun test2(): String {
        return "Hello world2"
    }

    @PostMapping("transaction")
    fun doTransaction(
        @RequestHeader("X-header1")
        header1: String,
        @RequestHeader("X-header2")
        header2: String,
        @RequestParam("param1")
        param1: String,
        @RequestParam("param2")
        param2: Long,
        @RequestBody
        body: TransactionRequest
    ) : ResponseEntity<TransactionResponse> {
        println("================================Controller======================================")
        println("Header1 : $header1")
        println("Header2 : $header2")
        println("param1  : $param1")
        println("param2  : $param2")
        println("body    : $body")

        val response = TransactionResponse(
            "resp1",
            0,
            false
        )
        val respHeader = HttpHeaders()
        respHeader.add("X-repshdr1", "Resp Hdr1")
        respHeader.add("X-repshdr2", "Resp Hdr2")
        println("================================End Controller======================================")

        return ResponseEntity(response, respHeader, HttpStatus.OK)
    }
}