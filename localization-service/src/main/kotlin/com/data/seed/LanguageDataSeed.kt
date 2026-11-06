package com.data.seed

import com.config.props.SeedProps
import com.data.entity.Language
import com.data.repository.LanguageRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
@Order(1)
class LanguageDataSeed(
    private val languageRepository: LanguageRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (languageRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/language.json")
        val dtos: Array<LanguageDto> = jacksonObjectMapper().readValue(json)
        languageRepository.saveAll(dtos.map { Language(code = it.code, name = it.name, enabled = it.enabled) })
    }

    private data class LanguageDto(
        @param:JsonProperty("code")    val code: String,
        @param:JsonProperty("name")    val name: String,
        @param:JsonProperty("enabled") val enabled: Boolean = true
    )
}
