package com.rest.controller.discount.data

import com.data.entity.DiscountStatus
import com.data.entity.DiscountType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class DiscountResponse(
    val id: UUID,
    val storeId: UUID,
    val productId: UUID?,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val startsAt: Instant,
    val endsAt: Instant?,
    val status: DiscountStatus
)
