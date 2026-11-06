package com.service.translation.dto

import com.data.entity.EntityType
import java.util.UUID

data class CreateTranslationDto(
    val entityType: EntityType,
    val entityId: UUID,
    val fieldName: String,
    val languageCode: String,
    val translatedValue: String
)
