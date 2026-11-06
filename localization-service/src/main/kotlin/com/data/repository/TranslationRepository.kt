package com.data.repository

import com.data.entity.EntityType
import com.data.entity.Translation
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TranslationRepository : JpaRepository<Translation, UUID> {
    fun findByEntityTypeAndEntityId(entityType: EntityType, entityId: UUID): List<Translation>
    fun findByEntityTypeAndEntityIdAndFieldNameAndLanguageCode(
        entityType: EntityType,
        entityId: UUID,
        fieldName: String,
        languageCode: String
    ): Translation?
}
