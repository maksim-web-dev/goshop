package com.rest.controller.translation.data

import com.data.entity.TranslationStatus
import jakarta.validation.constraints.Size

data class TranslationUpdateRequest(
    @field:Size(min = 1) val translatedValue: String? = null,
    val status: TranslationStatus? = null
)
