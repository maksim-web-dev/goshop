package com.rest.controller.translation

import com.data.entity.EntityType
import com.rest.controller.translation.adapter.TranslationCreateDtoAdapter
import com.rest.controller.translation.adapter.TranslationResponseAdapter
import com.rest.controller.translation.adapter.TranslationUpdateDtoAdapter
import com.rest.controller.translation.data.BatchResolveRequest
import com.rest.controller.translation.data.BatchResolveResponse
import com.rest.controller.translation.data.ResolveTranslationRequest
import com.rest.controller.translation.data.TranslationCreateRequest
import com.rest.controller.translation.data.TranslationResponse
import com.rest.controller.translation.data.TranslationUpdateRequest
import com.service.translation.TranslationService
import com.service.translation.dto.BatchResolveDto
import com.service.translation.dto.ResolveTranslationDto
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/translations")
class TranslationController(private val translationService: TranslationService) {

    @GetMapping
    fun getAll(
        @RequestParam entityType: EntityType?,
        @RequestParam entityId: Long?,
        pageable: Pageable
    ): Page<TranslationResponse> =
        with(TranslationResponseAdapter) { translationService.findAll(entityType, entityId, pageable).map { it.toApiModel() } }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: TranslationCreateRequest): TranslationResponse {
        val dto = with(TranslationCreateDtoAdapter) { request.toServiceModel() }
        return with(TranslationResponseAdapter) { translationService.create(dto).toApiModel() }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: TranslationUpdateRequest): TranslationResponse {
        val dto = with(TranslationUpdateDtoAdapter) { request.toServiceModel() }
        return with(TranslationResponseAdapter) { translationService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = translationService.delete(id)

    @PostMapping("/resolve")
    fun resolve(@Valid @RequestBody request: ResolveTranslationRequest): TranslationResponse {
        val dto = ResolveTranslationDto(
            entityType = request.entityType,
            entityId = request.entityId,
            fieldName = request.fieldName,
            preferredLanguage = request.preferredLanguage,
            fallbackLanguage = request.fallbackLanguage
        )
        return with(TranslationResponseAdapter) { translationService.resolve(dto).toApiModel() }
    }

    @PostMapping("/batch-resolve")
    fun batchResolve(@Valid @RequestBody request: BatchResolveRequest): BatchResolveResponse {
        val result = translationService.batchResolve(
            BatchResolveDto(
                entityType = request.entityType,
                entityIds = request.entityIds,
                fieldNames = request.fieldNames,
                languageCode = request.languageCode
            )
        )
        return BatchResolveResponse(
            translations = result.found.map {
                BatchResolveResponse.TranslationItem(
                    entityId = it.entityId,
                    fieldName = it.fieldName,
                    languageCode = request.languageCode,
                    value = it.value,
                    found = true
                )
            },
            missingTranslations = result.missing.map {
                BatchResolveResponse.MissingTranslationItem(
                    entityId = it.entityId,
                    fieldName = it.fieldName,
                    languageCode = request.languageCode
                )
            }
        )
    }
}
