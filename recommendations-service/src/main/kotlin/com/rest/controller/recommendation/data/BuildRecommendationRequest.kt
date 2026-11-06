package com.rest.controller.recommendation.data

import java.util.UUID

data class BuildRecommendationRequest(
    val productIds: List<UUID>
)
