package com.rest.controller.price.data

import java.util.UUID

data class CalculatePriceRequest(
    val productId: UUID,
    val storeId: UUID? = null,
    val currencyCode: String
)
