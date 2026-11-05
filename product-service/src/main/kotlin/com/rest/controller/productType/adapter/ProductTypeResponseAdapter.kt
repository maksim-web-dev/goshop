package com.rest.controller.productType.adapter

import com.data.entity.ProductType
import com.rest.controller.productType.data.ProductTypeResponse
import com.rest.util.ApiTypeAdapter

object ProductTypeResponseAdapter : ApiTypeAdapter<ProductTypeResponse, ProductType>() {

    override fun ProductType.toApiModel() = ProductTypeResponse(
        id = id,
        categoryId = category.id,
        name = name
    )
}