package com.service.search.dto

data class IndexProductDto(
    val productId: Long,
    val sku: String,
    val name: String,
    val localizedNames: Map<String, String> = emptyMap(),
    val brand: String,
    val categoryId: Long,
    val attributes: Map<String, String> = emptyMap(),
    val status: String
)
