package com.rest.controller.translationJob.data

import com.data.entity.EntityType
import com.data.entity.TranslationJobStatus
import java.time.Instant
import java.util.UUID

data class TranslationJobResponse(
    val id: UUID,
    val entityType: EntityType,
    val entityId: UUID,
    val targetLanguageCode: String,
    val status: TranslationJobStatus,
    val attempts: Int,
    val createdAt: Instant,
    val completedAt: Instant?
)
