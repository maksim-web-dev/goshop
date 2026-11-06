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

@Component
@Transactional
@Order(3)
class BrandDataSeed(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM brands", Long::class.java) ?: 0L
        if (count > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_brand.json")
        val dtos: Array<BrandDto> = jacksonObjectMapper().readValue(json)
        dtos.forEach { dto ->
            jdbc.update(
                "INSERT INTO brands (id, name, website_url) VALUES (?, ?, ?) ON CONFLICT (id) DO NOTHING",
                dto.id, dto.name, dto.websiteUrl
            )
        }
        jdbc.queryForObject(
            "SELECT setval(pg_get_serial_sequence('brands', 'id'), COALESCE((SELECT MAX(id) FROM brands), 1))",
            Long::class.java
        )
    }

    private data class BrandDto(
        @param:JsonProperty("id")          val id: Long,
        @param:JsonProperty("name")        val name: String,
        @param:JsonProperty("website_url") val websiteUrl: String? = null
    )
}
