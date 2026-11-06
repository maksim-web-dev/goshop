package com.data.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "prices")
class Price(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // ID товару з product-service
    @Column(name = "product_id", nullable = false)
    val productId: Long,

    // null = базова ціна платформи; не null = ціна конкретного магазину
    @Column(name = "store_id")
    val storeId: Long? = null,

    @Column(nullable = false, precision = 19, scale = 4)
    val amount: BigDecimal,

    @Column(name = "currency_code", nullable = false, length = 3)
    val currencyCode: String,

    @Column(name = "valid_from", nullable = false)
    val validFrom: Instant = Instant.now(),

    @Column(name = "valid_to")
    val validTo: Instant? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)