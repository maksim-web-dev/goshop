package com.rest.controller.recommendationRule.data

data class RecommendationRuleUpdateRequest(
    val name: String? = null,
    val parameters: String? = null,
    val priority: Int? = null,
    val enabled: Boolean? = null
)
