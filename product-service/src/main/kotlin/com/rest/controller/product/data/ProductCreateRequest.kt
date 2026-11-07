package com.rest.controller.product.data

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class ProductCreateRequest(
    @field:NotBlank val sku: String,
    @field:NotBlank val name: String,
    @field:Positive val brandId: Long,
    @field:Positive val categoryId: Long,
    @field:Positive val productTypeId: Long,
    val description: String? = null
)