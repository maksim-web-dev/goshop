package com.service.language

import com.data.entity.Language
import com.data.repository.LanguageRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class LanguageService(private val languageRepository: LanguageRepository) {

    fun findAll(): List<Language> = languageRepository.findByEnabledTrue()

    fun findByCode(code: String): Language =
        languageRepository.findById(code)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Language $code not found") }
}
