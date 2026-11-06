package com.rest.controller.discount.data

import com.data.entity.DiscountStatus
import com.data.entity.DiscountType
import java.math.BigDecimal
import java.time.Instant

data class DiscountResponse(
    val id: Long?,
    val storeId: Long,
    val productId: Long?,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val startsAt: Instant,
    val endsAt: Instant?,
    val status: DiscountStatus
)
