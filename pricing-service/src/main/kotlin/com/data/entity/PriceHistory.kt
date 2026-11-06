package com.data.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "price_history")
class PriceHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "price_id", nullable = false)
    val price: Price,

    @Column(name = "old_amount", nullable = false, precision = 19, scale = 4)
    val oldAmount: BigDecimal,

    @Column(name = "new_amount", nullable = false, precision = 19, scale = 4)
    val newAmount: BigDecimal,

    @Column(name = "changed_at", nullable = false, updatable = false)
    val changedAt: Instant = Instant.now()
)