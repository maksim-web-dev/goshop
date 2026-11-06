package com.service.translation.dto

import com.data.entity.EntityType
import java.util.UUID

data class ResolveTranslationDto(
    val entityType: EntityType,
    val entityId: UUID,
    val fieldName: String,
    val preferredLanguage: String,
    val fallbackLanguage: String = "en"
)
