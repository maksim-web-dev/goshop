package com.rest.controller.recommendationRule.data

import com.data.entity.RecommendationStrategy
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RecommendationRuleCreateRequest(
    @field:NotBlank @field:Size(max = 255) val name: String,
    val strategy: RecommendationStrategy,
    val parameters: String? = null,
    val priority: Int = 0,
    val enabled: Boolean = true
)
