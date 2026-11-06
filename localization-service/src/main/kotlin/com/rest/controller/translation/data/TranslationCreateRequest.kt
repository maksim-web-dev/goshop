package com.rest.controller.translation.data

import com.data.entity.EntityType

data class TranslationCreateRequest(
    val entityType: EntityType,
    val entityId: Long,
    val fieldName: String,
    val languageCode: String,
    val translatedValue: String
)
