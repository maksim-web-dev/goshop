package com.rest.controller.translation

import com.data.entity.EntityType
import com.rest.controller.translation.adapter.TranslationCreateDtoAdapter
import com.rest.controller.translation.adapter.TranslationResponseAdapter
import com.rest.controller.translation.adapter.TranslationUpdateDtoAdapter
import com.rest.controller.translation.data.ResolveTranslationRequest
import com.rest.controller.translation.data.TranslationCreateRequest
import com.rest.controller.translation.data.TranslationResponse
import com.rest.controller.translation.data.TranslationUpdateRequest
import com.service.translation.TranslationService
import com.service.translation.dto.ResolveTranslationDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/translations")
class TranslationController(private val translationService: TranslationService) {

    @GetMapping
    fun getAll(
        @RequestParam entityType: EntityType?,
        @RequestParam entityId: UUID?
    ): List<TranslationResponse> =
        with(TranslationResponseAdapter) { translationService.findAll(entityType, entityId).toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: TranslationCreateRequest): TranslationResponse {
        val dto = with(TranslationCreateDtoAdapter) { request.toServiceModel() }
        return with(TranslationResponseAdapter) { translationService.create(dto).toApiModel() }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody request: TranslationUpdateRequest): TranslationResponse {
        val dto = with(TranslationUpdateDtoAdapter) { request.toServiceModel() }
        return with(TranslationResponseAdapter) { translationService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) = translationService.delete(id)

    @PostMapping("/resolve")
    fun resolve(@RequestBody request: ResolveTranslationRequest): TranslationResponse {
        val dto = ResolveTranslationDto(
            entityType = request.entityType,
            entityId = request.entityId,
            fieldName = request.fieldName,
            preferredLanguage = request.preferredLanguage,
            fallbackLanguage = request.fallbackLanguage
        )
        return with(TranslationResponseAdapter) { translationService.resolve(dto).toApiModel() }
    }
}
