package com.rest.controller.product.adapter

import com.rest.controller.product.data.ProductUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.product.dto.UpdateProductDto

object ProductUpdateDtoAdapter : ApiTypeAdapter<ProductUpdateRequest, UpdateProductDto>() {

    override fun ProductUpdateRequest.toServiceModel() = UpdateProductDto(
        name = name,
        brandId = brandId,
        categoryId = categoryId,
        productTypeId = productTypeId,
        description = description,
        status = status
    )
}