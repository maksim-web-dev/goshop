package com.rest.controller.translationJob.adapter

import com.rest.controller.translationJob.data.TranslationJobCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.translationJob.dto.CreateTranslationJobDto

object TranslationJobCreateDtoAdapter : ApiTypeAdapter<TranslationJobCreateRequest, CreateTranslationJobDto>() {
    override fun TranslationJobCreateRequest.toServiceModel() = CreateTranslationJobDto(
        entityType = entityType,
        entityId = entityId,
        targetLanguageCode = targetLanguageCode
    )
}
