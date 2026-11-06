package com.data.repository

import com.data.entity.EntityType
import com.data.entity.Translation
import org.springframework.data.jpa.repository.JpaRepository

interface TranslationRepository : JpaRepository<Translation, Long> {
    fun findByEntityTypeAndEntityId(entityType: EntityType, entityId: Long): List<Translation>
    fun findByEntityTypeAndEntityIdAndFieldNameAndLanguageCode(
        entityType: EntityType,
        entityId: Long,
        fieldName: String,
        languageCode: String
    ): Translation?
}
