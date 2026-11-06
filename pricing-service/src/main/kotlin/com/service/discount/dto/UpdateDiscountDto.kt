package com.service.discount.dto

import com.data.entity.DiscountStatus
import java.math.BigDecimal
import java.time.Instant

data class UpdateDiscountDto(
    val discountValue: BigDecimal? = null,
    val startsAt: Instant? = null,
    val endsAt: Instant? = null,
    val status: DiscountStatus? = null
)
