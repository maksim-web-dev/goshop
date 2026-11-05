package com.rest.controller.productAttribute.adapter

import com.rest.controller.productAttribute.data.ProductAttributeSetRequest
import com.rest.util.ApiTypeAdapter
import com.service.productAttribute.dto.AttributeValueDto
import com.service.productAttribute.dto.SetAttributesDto

object ProductAttributeSetDtoAdapter : ApiTypeAdapter<ProductAttributeSetRequest, SetAttributesDto>() {

    override fun ProductAttributeSetRequest.toServiceModel() = SetAttributesDto(
        attributes = attributes.map { AttributeValueDto(categoryParameterId = it.categoryParameterId, value = it.value) }
    )
}