package com.rest.controller.translation.data

import com.data.entity.EntityType

data class ResolveTranslationRequest(
    val entityType: EntityType,
    val entityId: Long,
    val fieldName: String,
    val preferredLanguage: String,
    val fallbackLanguage: String = "en"
)
