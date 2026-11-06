package com.rest.controller.translation.adapter

import com.rest.controller.translation.data.TranslationUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.translation.dto.UpdateTranslationDto

object TranslationUpdateDtoAdapter : ApiTypeAdapter<TranslationUpdateRequest, UpdateTranslationDto>() {
    override fun TranslationUpdateRequest.toServiceModel() = UpdateTranslationDto(
        translatedValue = translatedValue,
        status = status
    )
}
