package com.rest.controller.product.data

import com.data.entity.ProductStatus
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class ProductUpdateRequest(
    @field:Size(min = 1) val name: String? = null,
    @field:Positive val brandId: Long? = null,
    @field:Positive val categoryId: Long? = null,
    @field:Positive val productTypeId: Long? = null,
    val description: String? = null,
    val status: ProductStatus? = null
)