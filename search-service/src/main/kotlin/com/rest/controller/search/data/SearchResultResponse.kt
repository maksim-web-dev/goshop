package com.rest.controller.search.data

import java.time.Instant
import java.util.UUID

data class SearchResultResponse(
    val productId: UUID,
    val sku: String,
    val name: String,
    val brand: String,
    val categoryId: UUID,
    val status: String,
    val indexedAt: Instant
)
