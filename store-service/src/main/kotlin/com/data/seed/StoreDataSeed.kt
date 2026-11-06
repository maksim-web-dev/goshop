package com.data.seed

import com.config.props.SeedProps
import com.data.entity.Store
import com.data.repository.StoreRepository
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
class StoreDataSeed(
    private val storeRepository: StoreRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (storeRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/store.json")
        val dtos: Array<StoreDto> = jacksonObjectMapper().readValue(json)
        storeRepository.saveAll(dtos.map {
            Store(
                name = it.name,
                domain = it.domain,
                countryCode = it.countryCode,
                defaultLanguage = it.defaultLanguage,
                defaultCurrency = it.defaultCurrency
            )
        })
    }

    private data class StoreDto(
        @param:JsonProperty("name")             val name: String,
        @param:JsonProperty("domain")           val domain: String,
        @param:JsonProperty("country_code")     val countryCode: String,
        @param:JsonProperty("default_language") val defaultLanguage: String,
        @param:JsonProperty("default_currency") val defaultCurrency: String
    )
}
