package com.rest.controller.price.data

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PriceResponse(
    val id: UUID,
    val productId: UUID,
    val storeId: UUID?,
    val amount: BigDecimal,
    val currencyCode: String,
    val validFrom: Instant,
    val validTo: Instant?,
    val updatedAt: Instant
)
