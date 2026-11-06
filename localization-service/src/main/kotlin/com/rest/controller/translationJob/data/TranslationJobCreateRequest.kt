package com.rest.controller.translationJob.data

import com.data.entity.EntityType
import java.util.UUID

data class TranslationJobCreateRequest(
    val entityType: EntityType,
    val entityId: UUID,
    val targetLanguageCode: String
)
