package com.rest.controller.price.data

import java.math.BigDecimal
import java.time.Instant

data class PriceResponse(
    val id: Long?,
    val productId: Long,
    val storeId: Long?,
    val amount: BigDecimal,
    val currencyCode: String,
    val validFrom: Instant,
    val validTo: Instant?,
    val updatedAt: Instant
)
