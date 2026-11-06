package com.service.recommendationRule.dto

import com.data.entity.RecommendationStrategy

data class CreateRecommendationRuleDto(
    val name: String,
    val strategy: RecommendationStrategy,
    val parameters: String? = null,
    val priority: Int = 0,
    val enabled: Boolean = true
)
