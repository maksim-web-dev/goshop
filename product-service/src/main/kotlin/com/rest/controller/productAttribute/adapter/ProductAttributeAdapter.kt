package com.rest.controller.productAttribute.adapter

import com.rest.controller.productAttribute.data.SetAttributesRequest
import com.service.productAttribute.dto.AttributeValueDto
import com.service.productAttribute.dto.SetAttributesDto

fun SetAttributesRequest.toDto() = SetAttributesDto(
    attributes = attributes.map { AttributeValueDto(categoryParameterId = it.categoryParameterId, value = it.value) }
)