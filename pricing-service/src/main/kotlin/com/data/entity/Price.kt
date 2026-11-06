package com.data.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "prices")
class Price(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    // UUID товару з product-service
    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    // null = базова ціна платформи; не null = ціна конкретного магазину
    @Column(name = "store_id")
    val storeId: UUID? = null,

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