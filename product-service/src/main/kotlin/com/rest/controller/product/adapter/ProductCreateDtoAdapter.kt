package com.rest.controller.product.adapter

import com.rest.controller.product.data.ProductCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.product.dto.CreateProductDto

object ProductCreateDtoAdapter : ApiTypeAdapter<ProductCreateRequest, CreateProductDto>() {

    override fun ProductCreateRequest.toServiceModel() = CreateProductDto(
        sku = sku,
        name = name,
        brandId = brandId,
        categoryId = categoryId,
        productTypeId = productTypeId,
        description = description
    )
}