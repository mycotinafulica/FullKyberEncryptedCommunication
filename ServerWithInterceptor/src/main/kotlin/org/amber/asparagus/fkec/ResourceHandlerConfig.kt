package org.amber.asparagus.fkec

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import java.util.Base64

@Configuration
class ResourceHandlerConfig {
    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Bean("privateKey")
    fun privateKey(): String {
        println("Initializing private key bean")

        val privateKeyResource = resourceLoader.getResource("classpath:private.p8")
        return Base64.getEncoder().encodeToString(privateKeyResource.contentAsByteArray)
    }
}