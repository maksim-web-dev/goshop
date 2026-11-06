package com.service.price.dto

import java.math.BigDecimal
import java.time.Instant

data class CreatePriceDto(
    val productId: Long,
    val storeId: Long? = null,
    val amount: BigDecimal,
    val currencyCode: String,
    val validFrom: Instant = Instant.now(),
    val validTo: Instant? = null
)
