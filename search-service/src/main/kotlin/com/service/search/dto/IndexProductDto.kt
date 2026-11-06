package com.service.search.dto

import java.util.UUID

data class IndexProductDto(
    val productId: UUID,
    val sku: String,
    val name: String,
    val localizedNames: Map<String, String> = emptyMap(),
    val brand: String,
    val categoryId: UUID,
    val attributes: Map<String, String> = emptyMap(),
    val status: String
)
