package com.data.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "exchange_rates")
class ExchangeRate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "from_currency", nullable = false, length = 3)
    val fromCurrency: String,

    @Column(name = "to_currency", nullable = false, length = 3)
    val toCurrency: String,

    @Column(nullable = false, precision = 19, scale = 8)
    val rate: BigDecimal,

    @Column(name = "effective_at", nullable = false)
    val effectiveAt: Instant = Instant.now()
)