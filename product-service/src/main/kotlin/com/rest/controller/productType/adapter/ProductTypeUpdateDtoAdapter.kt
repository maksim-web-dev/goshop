package com.rest.controller.productType.adapter

import com.rest.controller.productType.data.ProductTypeUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.productType.dto.UpdateProductTypeDto

object ProductTypeUpdateDtoAdapter : ApiTypeAdapter<ProductTypeUpdateRequest, UpdateProductTypeDto>() {

    override fun ProductTypeUpdateRequest.toServiceModel() = UpdateProductTypeDto(
        categoryId = categoryId,
        name = name
    )
}