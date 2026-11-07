package com.service.translation.dto

import com.data.entity.EntityType

data class BatchResolveDto(
    val entityType: EntityType,
    val entityIds: List<Long>,
    val fieldNames: List<String>,
    val languageCode: String
)
