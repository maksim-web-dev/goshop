package com.rest.controller.recommendationRule.adapter

import com.data.entity.RecommendationRule
import com.rest.controller.recommendationRule.data.RecommendationRuleResponse
import com.rest.util.ApiTypeAdapter

object RecommendationRuleResponseAdapter : ApiTypeAdapter<RecommendationRuleResponse, RecommendationRule>() {
    override fun RecommendationRule.toApiModel() = RecommendationRuleResponse(
        id = id, name = name, strategy = strategy, parameters = parameters, priority = priority, enabled = enabled
    )
}
