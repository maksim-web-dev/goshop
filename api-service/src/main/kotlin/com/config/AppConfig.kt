package com.config

import com.config.props.DownstreamProps
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
@EnableConfigurationProperties(DownstreamProps::class)
class AppConfig {

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(kotlinModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Bean
    fun restClientBuilder(props: DownstreamProps): RestClient.Builder {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofMillis(props.connectTimeoutMs))
            setReadTimeout(Duration.ofMillis(props.readTimeoutMs))
        }
        return RestClient.builder().requestFactory(factory)
    }
}
