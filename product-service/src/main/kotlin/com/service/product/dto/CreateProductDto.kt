package com.service.product.dto

data class CreateProductDto(
    val sku: String,
    val name: String,
    val brandId: Long,
    val categoryId: Long,
    val productTypeId: Long,
    val description: String? = null
)