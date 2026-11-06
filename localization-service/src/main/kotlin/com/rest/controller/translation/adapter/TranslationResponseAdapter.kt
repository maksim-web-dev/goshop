package com.rest.controller.translation.adapter

import com.data.entity.Translation
import com.rest.controller.translation.data.TranslationResponse
import com.rest.util.ApiTypeAdapter

object TranslationResponseAdapter : ApiTypeAdapter<TranslationResponse, Translation>() {
    override fun Translation.toApiModel() = TranslationResponse(
        id = id,
        entityType = entityType,
        entityId = entityId,
        fieldName = fieldName,
        languageCode = language.code,
        translatedValue = translatedValue,
        status = status,
        updatedAt = updatedAt
    )
}
