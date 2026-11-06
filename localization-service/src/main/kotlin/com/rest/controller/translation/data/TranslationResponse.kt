package com.rest.controller.translation.data

import com.data.entity.EntityType
import com.data.entity.TranslationStatus
import java.time.Instant

data class TranslationResponse(
    val id: Long?,
    val entityType: EntityType,
    val entityId: Long,
    val fieldName: String,
    val languageCode: String,
    val translatedValue: String,
    val status: TranslationStatus,
    val updatedAt: Instant
)
