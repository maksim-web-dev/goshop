package com.rest.controller.translation.data

import com.data.entity.TranslationStatus

data class TranslationUpdateRequest(
    val translatedValue: String? = null,
    val status: TranslationStatus? = null
)
