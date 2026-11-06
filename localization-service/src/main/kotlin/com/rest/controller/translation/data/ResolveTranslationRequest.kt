package com.rest.controller.translation.data

import com.data.entity.EntityType
import java.util.UUID

data class ResolveTranslationRequest(
    val entityType: EntityType,
    val entityId: UUID,
    val fieldName: String,
    val preferredLanguage: String,
    val fallbackLanguage: String = "en"
)
