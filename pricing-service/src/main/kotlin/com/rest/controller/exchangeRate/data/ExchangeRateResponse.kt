package com.rest.controller.exchangeRate.data

import java.math.BigDecimal
import java.time.Instant

data class ExchangeRateResponse(
    val id: Long?,
    val fromCurrency: String,
    val toCurrency: String,
    val rate: BigDecimal,
    val effectiveAt: Instant
)
