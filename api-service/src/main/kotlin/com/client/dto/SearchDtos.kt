package com.client.dto

import java.time.Instant

data class SearchResultResponse(
    val productId: Long,
    val sku: String,
    val name: String,
    val brand: String,
    val categoryId: Long,
    val status: String,
    val indexedAt: Instant
)
