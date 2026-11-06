package com.service.translation.dto

import com.data.entity.EntityType

data class ResolveTranslationDto(
    val entityType: EntityType,
    val entityId: Long,
    val fieldName: String,
    val preferredLanguage: String,
    val fallbackLanguage: String = "en"
)
