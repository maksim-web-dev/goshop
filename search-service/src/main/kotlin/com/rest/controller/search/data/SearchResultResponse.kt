package com.rest.controller.search.data

import java.time.Instant

data class SearchResultResponse(
    val productId: Long,
    val sku: String,
    val name: String,
    val localizedNames: Map<String, String> = emptyMap(),
    val brand: String,
    val categoryId: Long,
    val attributes: Map<String, String> = emptyMap(),
    val status: String,
    val indexedAt: Instant
)
