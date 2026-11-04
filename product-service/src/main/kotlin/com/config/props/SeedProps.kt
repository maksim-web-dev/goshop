package com.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.seed")
data class SeedProps (
    val mode: String = "",
    val stopCount: Int = 1,
    val enable: Boolean = false,
)
