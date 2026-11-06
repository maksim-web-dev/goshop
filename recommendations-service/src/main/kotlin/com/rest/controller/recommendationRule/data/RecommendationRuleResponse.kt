package com.rest.controller.recommendationRule.data

import com.data.entity.RecommendationStrategy

data class RecommendationRuleResponse(
    val id: Long?,
    val name: String,
    val strategy: RecommendationStrategy,
    val parameters: String?,
    val priority: Int,
    val enabled: Boolean
)
