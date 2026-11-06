package com.rest.controller.price.data

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PriceHistoryResponse(
    val id: UUID,
    val priceId: UUID,
    val oldAmount: BigDecimal,
    val newAmount: BigDecimal,
    val changedAt: Instant
)
