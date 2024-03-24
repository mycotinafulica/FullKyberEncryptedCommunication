package org.amber.asparagus.iolite.ServerWithInterceptor

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/iolite/api")
class MyController {

    @GetMapping("/test")
    fun test(): String {
        return "Hello world"
    }
}