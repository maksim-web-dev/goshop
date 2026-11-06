package com.rest.controller.price.data

import java.math.BigDecimal
import java.time.Instant

data class PriceCreateRequest(
    val productId: Long,
    val storeId: Long? = null,
    val amount: BigDecimal,
    val currencyCode: String,
    val validFrom: Instant = Instant.now(),
    val validTo: Instant? = null
)
