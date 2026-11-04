package com.rest.controller.product.data

data class CreateProductRequest(
    val sku: String,
    val name: String,
    val brandId: Long,
    val categoryId: Long,
    val productTypeId: Long,
    val description: String? = null
)