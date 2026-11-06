package com.data.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "discounts")
class Discount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "store_id", nullable = false)
    val storeId: Long,

    // null = знижка на весь асортимент магазину
    @Column(name = "product_id")
    val productId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    val discountType: DiscountType,

    // PERCENTAGE: 0–100, FIXED: сума в валюті магазину
    @Column(name = "discount_value", nullable = false, precision = 19, scale = 4)
    val discountValue: BigDecimal,

    @Column(name = "starts_at", nullable = false)
    val startsAt: Instant,

    @Column(name = "ends_at")
    val endsAt: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: DiscountStatus = DiscountStatus.ACTIVE
)