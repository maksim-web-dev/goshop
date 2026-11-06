package com.service.price.dto

import java.util.UUID

data class CalculatePriceDto(
    val productId: UUID,
    val storeId: UUID? = null,
    val currencyCode: String
)
