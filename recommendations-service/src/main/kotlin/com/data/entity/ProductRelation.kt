package com.data.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "product_relations",
    uniqueConstraints = [UniqueConstraint(columnNames = ["source_product_id", "target_product_id", "relation_type"])],
    indexes = [
        Index(name = "idx_product_relations_source_id", columnList = "source_product_id"),
        Index(name = "idx_product_relations_source_type", columnList = "source_product_id, relation_type")
    ]
)
class ProductRelation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "source_product_id", nullable = false)
    val sourceProductId: Long,

    @Column(name = "target_product_id", nullable = false)
    val targetProductId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false)
    val relationType: RelationType,

    // Ваговий коефіцієнт релевантності: 0.0–1.0
    @Column(nullable = false)
    val score: Double = 0.0
)