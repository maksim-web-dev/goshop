package com.data.seed

import com.config.props.SeedProps
import com.data.entity.StoreSettings
import com.data.repository.StoreRepository
import com.data.repository.StoreSettingsRepository
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
class StoreSettingsDataSeed(
    private val storeSettingsRepository: StoreSettingsRepository,
    private val storeRepository: StoreRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (storeSettingsRepository.count() > seedProps.stopCount) return

        val storesByDomain = storeRepository.findAll().associateBy { it.domain }

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/store_settings.json")
        val dtos: Array<StoreSettingsDto> = jacksonObjectMapper().readValue(json)
        storeSettingsRepository.saveAll(dtos.map {
            val store = storesByDomain[it.storeDomain]
                ?: throw IllegalArgumentException("Store '${it.storeDomain}' not found")
            StoreSettings(
                store = store,
                requestsPerSecond = it.requestsPerSecond,
                burstLimit = it.burstLimit,
                allowedLanguages = it.allowedLanguages.toMutableList()
            )
        })
    }

    private data class StoreSettingsDto(
        @param:JsonProperty("store_domain")          val storeDomain: String,
        @param:JsonProperty("requests_per_second")   val requestsPerSecond: Int = 100,
        @param:JsonProperty("burst_limit")           val burstLimit: Int = 200,
        @param:JsonProperty("allowed_languages")     val allowedLanguages: List<String> = listOf("uk", "en")
    )
}
