package com.rest.controller.productAttribute.adapter

import com.data.entity.ProductAttribute
import com.rest.controller.productAttribute.data.ProductAttributeResponse
import com.rest.util.ApiTypeAdapter

object ProductAttributeResponseAdapter : ApiTypeAdapter<ProductAttributeResponse, ProductAttribute>() {

    override fun ProductAttribute.toApiModel() = ProductAttributeResponse(
        id = id,
        productId = product.id,
        categoryParameterId = categoryParameter.id,
        value = value
    )
}