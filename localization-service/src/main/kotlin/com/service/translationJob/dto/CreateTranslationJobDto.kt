package com.service.translationJob.dto

import com.data.entity.EntityType

data class CreateTranslationJobDto(
    val entityType: EntityType,
    val entityId: Long,
    val targetLanguageCode: String
)
