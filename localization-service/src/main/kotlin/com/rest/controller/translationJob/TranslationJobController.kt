package com.rest.controller.translationJob

import com.rest.controller.translationJob.adapter.TranslationJobCreateDtoAdapter
import com.rest.controller.translationJob.adapter.TranslationJobResponseAdapter
import com.rest.controller.translationJob.data.TranslationJobCreateRequest
import com.rest.controller.translationJob.data.TranslationJobResponse
import com.service.translationJob.TranslationJobService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/translation-jobs")
class TranslationJobController(private val translationJobService: TranslationJobService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: TranslationJobCreateRequest): TranslationJobResponse {
        val dto = with(TranslationJobCreateDtoAdapter) { request.toServiceModel() }
        return with(TranslationJobResponseAdapter) { translationJobService.create(dto).toApiModel() }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): TranslationJobResponse =
        with(TranslationJobResponseAdapter) { translationJobService.findById(id).toApiModel() }
}
