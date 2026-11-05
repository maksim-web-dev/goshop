package com.rest.controller.productAttribute.data

data class ProductAttributeResponse(
    val id: Long,
    val productId: Long,
    val categoryParameterId: Long,
    val value: String
)