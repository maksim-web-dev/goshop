package com.data.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "product_relations",
    uniqueConstraints = [UniqueConstraint(columnNames = ["source_product_id", "target_product_id", "relation_type"])]
)
class ProductRelation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "source_product_id", nullable = false)
    val sourceProductId: UUID,

    @Column(name = "target_product_id", nullable = false)
    val targetProductId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false)
    val relationType: RelationType,

    // Ваговий коефіцієнт релевантності: 0.0–1.0
    @Column(nullable = false)
    val score: Double = 0.0
)