package com.data.seed

import com.config.props.SeedProps
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
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
class SearchDocumentDataSeed(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM search_documents", Long::class.java) ?: 0L
        if (count > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/search_document.json")
        val dtos: Array<SearchDocumentDto> = jacksonObjectMapper().readValue(json)
        dtos.forEach { dto ->
            val searchableText = dto.searchableText ?: listOf(dto.name, dto.brand, dto.sku).joinToString(" ")
            jdbc.update(
                """
                INSERT INTO search_documents (product_id, sku, name, brand, category_id, searchable_text, localized_names, attributes, status, indexed_at)
                VALUES (?, ?, ?, ?, ?, to_tsvector('simple', ?), ?::jsonb, ?::jsonb, ?, ?)
                ON CONFLICT (product_id) DO NOTHING
                """.trimIndent(),
                dto.productId, dto.sku, dto.name, dto.brand, dto.categoryId,
                searchableText,
                dto.localizedNames?.toString(), dto.attributes?.toString(),
                dto.status, Instant.now()
            )
        }
    }

    private data class SearchDocumentDto(
        @param:JsonProperty("product_id")      val productId: Long,
        @param:JsonProperty("sku")             val sku: String,
        @param:JsonProperty("name")            val name: String,
        @param:JsonProperty("brand")           val brand: String,
        @param:JsonProperty("category_id")     val categoryId: Long,
        @param:JsonProperty("status")          val status: String = "ACTIVE",
        @param:JsonProperty("localized_names") val localizedNames: JsonNode? = null,
        @param:JsonProperty("attributes")      val attributes: JsonNode? = null,
        @param:JsonProperty("searchable_text") val searchableText: String? = null
    )
}
