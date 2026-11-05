package com.rest.controller.productType.adapter

import com.rest.controller.productType.data.ProductTypeCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.productType.dto.CreateProductTypeDto

object ProductTypeCreateDtoAdapter : ApiTypeAdapter<ProductTypeCreateRequest, CreateProductTypeDto>() {

    override fun ProductTypeCreateRequest.toServiceModel() = CreateProductTypeDto(
        categoryId = categoryId,
        name = name
    )
}