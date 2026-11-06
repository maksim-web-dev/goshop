package com.rest.controller.discount.data

import com.data.entity.DiscountType
import java.math.BigDecimal
import java.time.Instant

data class DiscountCreateRequest(
    val storeId: Long,
    val productId: Long? = null,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val startsAt: Instant,
    val endsAt: Instant? = null
)
