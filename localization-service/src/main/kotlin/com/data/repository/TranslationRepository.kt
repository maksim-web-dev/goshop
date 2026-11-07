package com.data.repository

import com.data.entity.EntityType
import com.data.entity.Translation
import com.data.entity.TranslationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TranslationRepository : JpaRepository<Translation, Long> {
    fun findByEntityTypeAndEntityId(entityType: EntityType, entityId: Long): List<Translation>
    fun findByEntityTypeAndEntityId(entityType: EntityType, entityId: Long, pageable: Pageable): Page<Translation>
    fun findByEntityType(entityType: EntityType, pageable: Pageable): Page<Translation>
    fun findByEntityTypeAndEntityIdAndFieldNameAndLanguageCode(
        entityType: EntityType,
        entityId: Long,
        fieldName: String,
        languageCode: String
    ): Translation?

    @Query("""
        SELECT t FROM Translation t
        WHERE t.entityType = :entityType
          AND t.entityId IN :entityIds
          AND t.fieldName IN :fieldNames
          AND t.language.code = :languageCode
          AND t.status = :status
    """)
    fun findApprovedBatch(
        entityType: EntityType,
        entityIds: Collection<Long>,
        fieldNames: Collection<String>,
        languageCode: String,
        status: TranslationStatus = TranslationStatus.APPROVED
    ): List<Translation>
}
