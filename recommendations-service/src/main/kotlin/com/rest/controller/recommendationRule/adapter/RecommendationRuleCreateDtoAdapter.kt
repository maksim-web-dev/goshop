package com.rest.controller.recommendationRule.adapter

import com.rest.controller.recommendationRule.data.RecommendationRuleCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.recommendationRule.dto.CreateRecommendationRuleDto

object RecommendationRuleCreateDtoAdapter : ApiTypeAdapter<RecommendationRuleCreateRequest, CreateRecommendationRuleDto>() {
    override fun RecommendationRuleCreateRequest.toServiceModel() = CreateRecommendationRuleDto(
        name = name, strategy = strategy, parameters = parameters, priority = priority, enabled = enabled
    )
}
