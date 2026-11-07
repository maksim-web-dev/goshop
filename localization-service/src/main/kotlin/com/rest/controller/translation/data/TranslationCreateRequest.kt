package com.rest.controller.translation.data

import com.data.entity.EntityType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class TranslationCreateRequest(
    val entityType: EntityType,
    @field:Positive val entityId: Long,
    @field:NotBlank val fieldName: String,
    @field:NotBlank val languageCode: String,
    @field:NotBlank val translatedValue: String
)
