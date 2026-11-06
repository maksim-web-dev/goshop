package com.rest.controller.recommendationRule.data

import com.data.entity.RecommendationStrategy

data class RecommendationRuleCreateRequest(
    val name: String,
    val strategy: RecommendationStrategy,
    val parameters: String? = null,
    val priority: Int = 0,
    val enabled: Boolean = true
)
