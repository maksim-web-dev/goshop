package com.rest.controller.productAttribute.data

data class ProductAttributeSetRequest(
    val attributes: List<ProductAttributeItemRequest>
)

data class ProductAttributeItemRequest(
    val categoryParameterId: Long,
    val value: String
)