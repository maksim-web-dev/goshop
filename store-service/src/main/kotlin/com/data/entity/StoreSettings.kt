package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "store_settings")
class StoreSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    val store: Store,

    @Column(name = "requests_per_second", nullable = false)
    val requestsPerSecond: Int = 100,

    @Column(name = "burst_limit", nullable = false)
    val burstLimit: Int = 200,

    // BCP 47 коди дозволених мов: ["uk", "en", "de"]
    @ElementCollection
    @CollectionTable(
        name = "store_allowed_languages",
        joinColumns = [JoinColumn(name = "store_settings_id")]
    )
    @Column(name = "language_code", nullable = false, length = 10)
    val allowedLanguages: MutableList<String> = mutableListOf()
)