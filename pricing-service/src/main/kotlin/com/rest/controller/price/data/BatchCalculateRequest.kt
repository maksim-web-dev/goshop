package com.rest.controller.price.data

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class BatchCalculateRequest(
    @field:NotEmpty(message = "items must not be empty")
    @field:Size(max = 100, message = "items must not exceed 100 items")
    @field:Valid
    val items: List<BatchCalculateItem>
) {
    data class BatchCalculateItem(
        @field:Positive val productId: Long,
        val storeId: Long? = null,
        @field:NotBlank @field:Size(min = 3, max = 3) val currencyCode: String,
        @field:Size(min = 3, max = 3) val targetCurrencyCode: String? = null
    )
}
