package com.service.translationJob.dto

import com.data.entity.EntityType
import java.util.UUID

data class CreateTranslationJobDto(
    val entityType: EntityType,
    val entityId: UUID,
    val targetLanguageCode: String
)
