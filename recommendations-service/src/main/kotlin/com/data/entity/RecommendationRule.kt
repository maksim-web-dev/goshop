package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "recommendation_rules")
class RecommendationRule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

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