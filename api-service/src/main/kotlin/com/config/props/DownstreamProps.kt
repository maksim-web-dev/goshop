package com.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.downstream")
data class DownstreamProps(
    val productServiceUrl: String,
    val localizationServiceUrl: String,
    val pricingServiceUrl: String,
    val compatibilityServiceUrl: String,
    val searchServiceUrl: String,
    val recommendationsServiceUrl: String,
    val storeServiceUrl: String,
    val connectTimeoutMs: Long = 3_000,
    val readTimeoutMs: Long = 10_000
)
