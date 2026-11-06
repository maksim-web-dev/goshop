package com.data.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "recommendation_snapshots")
class RecommendationSnapshot(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val strategy: RecommendationStrategy,

    // Попередньо розраховані UUID рекомендованих товарів
    @ElementCollection
    @CollectionTable(
        name = "recommendation_snapshot_products",
        joinColumns = [JoinColumn(name = "snapshot_id")]
    )
    @Column(name = "recommended_product_id", nullable = false)
    @OrderColumn(name = "position")
    val recommendedProductIds: MutableList<UUID> = mutableListOf(),

    @Column(name = "generated_at", nullable = false, updatable = false)
    val generatedAt: Instant = Instant.now()
)