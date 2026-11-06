package com.service.translation.dto

import com.data.entity.EntityType

data class CreateTranslationDto(
    val entityType: EntityType,
    val entityId: Long,
    val fieldName: String,
    val languageCode: String,
    val translatedValue: String
)
