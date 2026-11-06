package com.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.datasource.postgres")
class PostgresProps(
    val host: String,
    val port: Int,
    val db: String,
    val user: String,
    val pass: String,
    val ssl: String,
    val poolMax: Int = 10,
    val poolMin: Int = 2,
    val poolTimeoutMillis: Long = 30_000,
    val showSql: Boolean = false,
    val ddlAuto: String = "none"
)
