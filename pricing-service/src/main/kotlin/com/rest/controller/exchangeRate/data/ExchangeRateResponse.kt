package com.rest.controller.exchangeRate.data

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ExchangeRateResponse(
    val id: UUID,
    val fromCurrency: String,
    val toCurrency: String,
    val rate: BigDecimal,
    val effectiveAt: Instant
)
