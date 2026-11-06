package com.rest.controller.price.data

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PriceCreateRequest(
    val productId: UUID,
    val storeId: UUID? = null,
    val amount: BigDecimal,
    val currencyCode: String,
    val validFrom: Instant = Instant.now(),
    val validTo: Instant? = null
)
