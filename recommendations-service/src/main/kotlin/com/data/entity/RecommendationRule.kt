package com.data.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "recommendation_rules")
class RecommendationRule(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val strategy: RecommendationStrategy,

    // JSONB: параметри конкретної стратегії (діапазон ціни, атрибути тощо)
    @Column(columnDefinition = "jsonb")
    val parameters: String? = null,

    @Column(nullable = false)
    val priority: Int = 0,

    @Column(nullable = false)
    val enabled: Boolean = true
)