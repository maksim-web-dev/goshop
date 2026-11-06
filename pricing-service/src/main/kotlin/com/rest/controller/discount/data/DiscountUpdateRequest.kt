package com.rest.controller.discount.data

import com.data.entity.DiscountStatus
import java.math.BigDecimal
import java.time.Instant

data class DiscountUpdateRequest(
    val discountValue: BigDecimal? = null,
    val startsAt: Instant? = null,
    val endsAt: Instant? = null,
    val status: DiscountStatus? = null
)
