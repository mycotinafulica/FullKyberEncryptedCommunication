package org.amber.asparagus.fkec.filter

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterRegistrar {

    @Bean
    fun kyberFilter(): FilterRegistrationBean<KyberFilter> {
        val registrationBean = FilterRegistrationBean<KyberFilter>()

        registrationBean.filter = KyberFilter()
        registrationBean.addUrlPatterns("/fkec/post-handshake/*")
        registrationBean.order = 100

        return registrationBean
    }
}