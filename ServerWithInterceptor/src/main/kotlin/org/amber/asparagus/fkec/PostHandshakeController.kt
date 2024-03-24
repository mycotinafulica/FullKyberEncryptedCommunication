package org.amber.asparagus.fkec

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}