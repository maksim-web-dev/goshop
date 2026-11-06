package com.rest.controller.translation.data

import com.data.entity.EntityType
import com.data.entity.TranslationStatus
import java.time.Instant
import java.util.UUID

data class TranslationResponse(
    val id: UUID,
    val entityType: EntityType,
    val entityId: UUID,
    val fieldName: String,
    val languageCode: String,
    val translatedValue: String,
    val status: TranslationStatus,
    val updatedAt: Instant
)
