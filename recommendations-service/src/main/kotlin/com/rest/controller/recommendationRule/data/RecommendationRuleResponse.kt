package com.rest.controller.recommendationRule.data

import com.data.entity.RecommendationStrategy
import java.util.UUID

data class RecommendationRuleResponse(
    val id: UUID,
    val name: String,
    val strategy: RecommendationStrategy,
    val parameters: String?,
    val priority: Int,
    val enabled: Boolean
)
