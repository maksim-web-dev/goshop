package com.data.seed

import com.config.props.SeedProps
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
@Transactional
@Order(1)
class StoreDataSeed(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM stores", Long::class.java) ?: 0L
        if (count > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/store_store.json")
        val dtos: Array<StoreDto> = jacksonObjectMapper().readValue(json)
        val now = Instant.now()
        dtos.forEach { dto ->
            jdbc.update(
                "INSERT INTO stores (id, name, domain, country_code, default_language, default_currency, status, created_at) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', ?) ON CONFLICT (id) DO NOTHING",
                dto.id, dto.name, dto.domain, dto.countryCode, dto.defaultLanguage, dto.defaultCurrency, now
            )
        }
        jdbc.queryForObject(
            "SELECT setval(pg_get_serial_sequence('stores', 'id'), COALESCE((SELECT MAX(id) FROM stores), 1))",
            Long::class.java
        )
    }

    private data class StoreDto(
        @param:JsonProperty("id")               val id: Long,
        @param:JsonProperty("name")             val name: String,
        @param:JsonProperty("domain")           val domain: String,
        @param:JsonProperty("country_code")     val countryCode: String,
        @param:JsonProperty("default_language") val defaultLanguage: String,
        @param:JsonProperty("default_currency") val defaultCurrency: String
    )
}
