package com.data.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "compatibility_checks")
class CompatibilityCheck(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // ID товарів з product-service, що перевіряються разом
    @ElementCollection
    @CollectionTable(
        name = "compatibility_check_products",
        joinColumns = [JoinColumn(name = "check_id")]
    )
    @Column(name = "product_id", nullable = false)
    var productIds: MutableList<Long> = mutableListOf(),

    @Column(nullable = false)
    var compatible: Boolean,

    @Column(name = "checked_at", nullable = false, updatable = false)
    var checkedAt: Instant = Instant.now()
)