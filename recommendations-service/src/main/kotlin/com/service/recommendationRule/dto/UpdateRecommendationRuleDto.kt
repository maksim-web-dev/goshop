package com.service.recommendationRule.dto

data class UpdateRecommendationRuleDto(
    val name: String? = null,
    val parameters: String? = null,
    val priority: Int? = null,
    val enabled: Boolean? = null
)
