package com.client.dto

data class ApiKeyValidateRequest(val apiKey: String)

data class ApiKeyValidateResponse(
    val valid: Boolean,
    val storeId: Long? = null,
    val storeStatus: String? = null,
    val defaultLanguage: String? = null,
    val defaultCurrency: String? = null,
    val requestsPerSecond: Int? = null,
    val burstLimit: Int? = null
)
