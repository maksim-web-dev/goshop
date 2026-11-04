package com.rest.controller.productAttribute.data

data class SetAttributesRequest(
    val attributes: List<AttributeValueRequest>
)

data class AttributeValueRequest(
    val categoryParameterId: Long,
    val value: String
)