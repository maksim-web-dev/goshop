package com.rest.controller.price.data

import java.math.BigDecimal
import java.time.Instant

data class PriceHistoryResponse(
    val id: Long?,
    val priceId: Long?,
    val oldAmount: BigDecimal,
    val newAmount: BigDecimal,
    val changedAt: Instant
)
