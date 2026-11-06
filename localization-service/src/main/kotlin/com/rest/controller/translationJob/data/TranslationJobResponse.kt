package com.rest.controller.translationJob.data

import com.data.entity.EntityType
import com.data.entity.TranslationJobStatus
import java.time.Instant

data class TranslationJobResponse(
    val id: Long?,
    val entityType: EntityType,
    val entityId: Long,
    val targetLanguageCode: String,
    val status: TranslationJobStatus,
    val attempts: Int,
    val createdAt: Instant,
    val completedAt: Instant?
)
