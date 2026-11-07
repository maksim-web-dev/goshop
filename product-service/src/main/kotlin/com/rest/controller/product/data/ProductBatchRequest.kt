package com.rest.controller.product.data

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class ProductBatchRequest(
    @field:NotEmpty(message = "productIds must not be empty")
    @field:Size(max = 100, message = "productIds must not exceed 100 items")
    val productIds: List<Long> = emptyList()
)
