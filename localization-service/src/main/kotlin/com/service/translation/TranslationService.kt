package com.service.translation

import com.data.entity.EntityType
import com.data.entity.Translation
import com.data.entity.TranslationStatus
import com.data.repository.LanguageRepository
import com.data.repository.TranslationRepository
import com.service.translation.dto.BatchResolveDto
import com.service.translation.dto.BatchResolveResultDto
import com.service.translation.dto.CreateTranslationDto
import com.service.translation.dto.ResolveTranslationDto
import com.service.translation.dto.UpdateTranslationDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class TranslationService(
    private val translationRepository: TranslationRepository,
    private val languageRepository: LanguageRepository
) {

    fun findAll(entityType: EntityType?, entityId: Long?, pageable: Pageable): Page<Translation> {
        if (entityType != null && entityId != null)
            return translationRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
        if (entityType != null)
            return translationRepository.findByEntityType(entityType, pageable)
        return translationRepository.findAll(pageable)
    }

    fun findById(id: Long): Translation =
        translationRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Translation $id not found") }

    @Transactional
    fun create(dto: CreateTranslationDto): Translation {
        val language = languageRepository.findById(dto.languageCode)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Language ${dto.languageCode} not found") }
        return translationRepository.save(
            Translation(
                entityType = dto.entityType,
                entityId = dto.entityId,
                fieldName = dto.fieldName,
                language = language,
                translatedValue = dto.translatedValue
            )
        )
    }

    @Transactional
    fun update(id: Long, dto: UpdateTranslationDto): Translation {
        val existing = findById(id)
        return translationRepository.save(
            Translation(
                id = existing.id,
                entityType = existing.entityType,
                entityId = existing.entityId,
                fieldName = existing.fieldName,
                language = existing.language,
                translatedValue = dto.translatedValue ?: existing.translatedValue,
                status = dto.status ?: existing.status,
                updatedAt = Instant.now()
            )
        )
    }

    @Transactional
    fun delete(id: Long) {
        if (!translationRepository.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Translation $id not found")
        translationRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun batchResolve(dto: BatchResolveDto): BatchResolveResultDto {
        val language = languageRepository.findById(dto.languageCode)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Language '${dto.languageCode}' not found") }
        if (!language.enabled)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Language '${dto.languageCode}' is disabled")

        val found = translationRepository.findApprovedBatch(dto.entityType, dto.entityIds, dto.fieldNames, dto.languageCode)
        val foundIndex = found.associateBy { it.entityId to it.fieldName }

        val missing = dto.entityIds.flatMap { entityId ->
            dto.fieldNames
                .filter { fieldName -> (entityId to fieldName) !in foundIndex }
                .map { fieldName -> BatchResolveResultDto.MissingItem(entityId, fieldName) }
        }

        return BatchResolveResultDto(
            found = found.map { BatchResolveResultDto.FoundItem(it.entityId, it.fieldName, it.translatedValue) },
            missing = missing
        )
    }

    fun resolve(dto: ResolveTranslationDto): Translation {
        return translationRepository.findByEntityTypeAndEntityIdAndFieldNameAndLanguageCode(
            dto.entityType, dto.entityId, dto.fieldName, dto.preferredLanguage
        ) ?: translationRepository.findByEntityTypeAndEntityIdAndFieldNameAndLanguageCode(
            dto.entityType, dto.entityId, dto.fieldName, dto.fallbackLanguage
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No translation found for ${dto.entityType}/${dto.entityId}/${dto.fieldName}"
        )
    }
}
