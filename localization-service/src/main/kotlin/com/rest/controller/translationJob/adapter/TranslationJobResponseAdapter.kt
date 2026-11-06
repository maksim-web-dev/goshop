package com.rest.controller.translationJob.adapter

import com.data.entity.TranslationJob
import com.rest.controller.translationJob.data.TranslationJobResponse
import com.rest.util.ApiTypeAdapter

object TranslationJobResponseAdapter : ApiTypeAdapter<TranslationJobResponse, TranslationJob>() {
    override fun TranslationJob.toApiModel() = TranslationJobResponse(
        id = id,
        entityType = entityType,
        entityId = entityId,
        targetLanguageCode = targetLanguage.code,
        status = status,
        attempts = attempts,
        createdAt = createdAt,
        completedAt = completedAt
    )
}
