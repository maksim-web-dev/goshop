package com.service.discount.dto

import com.data.entity.DiscountType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CreateDiscountDto(
    val storeId: UUID,
    val productId: UUID? = null,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val startsAt: Instant,
    val endsAt: Instant? = null
)
