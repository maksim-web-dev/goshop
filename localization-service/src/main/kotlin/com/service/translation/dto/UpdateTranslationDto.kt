package com.service.translation.dto

import com.data.entity.TranslationStatus

data class UpdateTranslationDto(
    val translatedValue: String? = null,
    val status: TranslationStatus? = null
)
