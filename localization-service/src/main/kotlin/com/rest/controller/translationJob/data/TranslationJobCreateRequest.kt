package com.rest.controller.translationJob.data

import com.data.entity.EntityType

data class TranslationJobCreateRequest(
    val entityType: EntityType,
    val entityId: Long,
    val targetLanguageCode: String
)
