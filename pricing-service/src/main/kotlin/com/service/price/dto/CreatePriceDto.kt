package com.service.price.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CreatePriceDto(
    val productId: UUID,
    val storeId: UUID? = null,
    val amount: BigDecimal,
    val currencyCode: String,
    val validFrom: Instant = Instant.now(),
    val validTo: Instant? = null
)
