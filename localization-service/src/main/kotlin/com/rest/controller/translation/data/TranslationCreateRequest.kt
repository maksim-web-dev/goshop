package com.rest.controller.translation.data

import com.data.entity.EntityType
import java.util.UUID

data class TranslationCreateRequest(
    val entityType: EntityType,
    val entityId: UUID,
    val fieldName: String,
    val languageCode: String,
    val translatedValue: String
)
