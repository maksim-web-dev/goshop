package com.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = ["com"])
@EnableWebSecurity
@EnableConfigurationProperties
@ConfigurationPropertiesScan("com.config.props")
class AppConfig {

    @Bean
    fun pageableCustomizer(): PageableHandlerMethodArgumentResolverCustomizer =
        PageableHandlerMethodArgumentResolverCustomizer { it.setMaxPageSize(100) }
}
