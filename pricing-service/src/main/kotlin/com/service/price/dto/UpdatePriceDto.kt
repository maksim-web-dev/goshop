package com.service.price.dto

import java.math.BigDecimal
import java.time.Instant

data class UpdatePriceDto(
    val amount: BigDecimal? = null,
    val currencyCode: String? = null,
    val validFrom: Instant? = null,
    val validTo: Instant? = null
)
