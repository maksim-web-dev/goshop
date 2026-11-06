package com.rest.controller.language

import com.rest.controller.language.adapter.LanguageResponseAdapter
import com.rest.controller.language.data.LanguageResponse
import com.service.language.LanguageService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/languages")
class LanguageController(private val languageService: LanguageService) {

    @GetMapping
    fun getAll(): List<LanguageResponse> =
        with(LanguageResponseAdapter) { languageService.findAll().toApiModel() }
}
