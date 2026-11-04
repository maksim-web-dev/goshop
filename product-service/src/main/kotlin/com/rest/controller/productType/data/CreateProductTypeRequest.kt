package com.rest.controller.productType.data

data class CreateProductTypeRequest(
    val categoryId: Long,
    val name: String
)