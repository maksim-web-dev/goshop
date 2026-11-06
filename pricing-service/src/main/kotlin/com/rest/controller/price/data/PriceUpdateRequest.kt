package com.rest.controller.price.data

import java.math.BigDecimal
import java.time.Instant

data class PriceUpdateRequest(
    val amount: BigDecimal? = null,
    val currencyCode: String? = null,
    val validFrom: Instant? = null,
    val validTo: Instant? = null
)
