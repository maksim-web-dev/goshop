package com.filter

data class StoreContext(
    val storeId: Long,
    val storeStatus: String,
    val defaultLanguage: String,
    val defaultCurrency: String,
    val requestsPerSecond: Int,
    val burstLimit: Int
)

const val STORE_CONTEXT_ATTR = "storeContext"
const val API_KEY_HEADER = "X-Store-Api-Key"
