package com.service.translationJob

import com.data.entity.TranslationJob
import com.data.repository.LanguageRepository
import com.data.repository.TranslationJobRepository
import com.service.translationJob.dto.CreateTranslationJobDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class TranslationJobService(
    private val translationJobRepository: TranslationJobRepository,
    private val languageRepository: LanguageRepository
) {

    fun findById(id: UUID): TranslationJob =
        translationJobRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Job $id not found") }

    @Transactional
    fun create(dto: CreateTranslationJobDto): TranslationJob {
        val language = languageRepository.findById(dto.targetLanguageCode)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Language ${dto.targetLanguageCode} not found") }
        return translationJobRepository.save(
            TranslationJob(
                entityType = dto.entityType,
                entityId = dto.entityId,
                targetLanguage = language
            )
        )
    }
}
