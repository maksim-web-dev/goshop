package com.data.seed

import com.config.props.SeedProps
import com.data.entity.EntityType
import com.data.entity.Translation
import com.data.entity.TranslationStatus
import com.data.repository.LanguageRepository
import com.data.repository.TranslationRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
@Order(2)
class TranslationDataSeed(
    private val translationRepository: TranslationRepository,
    private val languageRepository: LanguageRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (translationRepository.count() > seedProps.stopCount) return

        val languagesByCode = languageRepository.findAll().associateBy { it.code }

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/localization_translation.json")
        val dtos: Array<TranslationDto> = jacksonObjectMapper().readValue(json)
        translationRepository.saveAll(dtos.map {
            val language = languagesByCode[it.languageCode]
                ?: throw IllegalArgumentException("Language '${it.languageCode}' not found")
            Translation(
                entityType = EntityType.valueOf(it.entityType),
                entityId = it.entityId,
                fieldName = it.fieldName,
                language = language,
                translatedValue = it.translatedValue,
                status = TranslationStatus.APPROVED
            )
        })
    }

    private data class TranslationDto(
        @param:JsonProperty("entity_type")       val entityType: String,
        @param:JsonProperty("entity_id")         val entityId: Long,
        @param:JsonProperty("field_name")        val fieldName: String,
        @param:JsonProperty("language_code")     val languageCode: String,
        @param:JsonProperty("translated_value")  val translatedValue: String
    )
}
