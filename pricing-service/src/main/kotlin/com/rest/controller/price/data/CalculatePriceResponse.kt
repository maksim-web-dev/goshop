package com.rest.controller.price.data

import java.math.BigDecimal

data class CalculatePriceResponse(
    val originalAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val currencyCode: String,
    val discountApplied: Boolean
)
