package com.rest.controller.productType.data

data class ProductTypeCreateRequest(
    val categoryId: Long,
    val name: String
)