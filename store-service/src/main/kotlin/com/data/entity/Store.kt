package com.data.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "stores")
class Store(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val domain: String,

    @Column(name = "country_code", nullable = false, length = 2)
    val countryCode: String,

    @Column(name = "default_language", nullable = false, length = 10)
    val defaultLanguage: String,

    @Column(name = "default_currency", nullable = false, length = 3)
    val defaultCurrency: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: StoreStatus = StoreStatus.ACTIVE,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)