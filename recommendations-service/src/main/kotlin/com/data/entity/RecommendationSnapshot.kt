package com.data.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "recommendation_snapshots")
class RecommendationSnapshot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val strategy: RecommendationStrategy,

    // Попередньо розраховані ID рекомендованих товарів
    @ElementCollection
    @CollectionTable(
        name = "recommendation_snapshot_products",
        joinColumns = [JoinColumn(name = "snapshot_id")]
    )
    @Column(name = "recommended_product_id", nullable = false)
    @OrderColumn(name = "position")
    val recommendedProductIds: MutableList<Long> = mutableListOf(),

    @Column(name = "generated_at", nullable = false, updatable = false)
    val generatedAt: Instant = Instant.now()
)