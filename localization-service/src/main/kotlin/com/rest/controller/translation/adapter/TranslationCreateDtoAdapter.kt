package com.rest.controller.translation.adapter

import com.rest.controller.translation.data.TranslationCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.translation.dto.CreateTranslationDto

object TranslationCreateDtoAdapter : ApiTypeAdapter<TranslationCreateRequest, CreateTranslationDto>() {
    override fun TranslationCreateRequest.toServiceModel() = CreateTranslationDto(
        entityType = entityType,
        entityId = entityId,
        fieldName = fieldName,
        languageCode = languageCode,
        translatedValue = translatedValue
    )
}
