package com.rest.controller.recommendationRule.adapter

import com.rest.controller.recommendationRule.data.RecommendationRuleUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.recommendationRule.dto.UpdateRecommendationRuleDto

object RecommendationRuleUpdateDtoAdapter : ApiTypeAdapter<RecommendationRuleUpdateRequest, UpdateRecommendationRuleDto>() {
    override fun RecommendationRuleUpdateRequest.toServiceModel() = UpdateRecommendationRuleDto(
        name = name, parameters = parameters, priority = priority, enabled = enabled
    )
}
