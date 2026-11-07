package com.service.search

import com.data.entity.SearchDocument
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.service.search.dto.IndexProductDto
import com.service.search.dto.SearchQueryDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SearchService(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate
) {
    private val mapper = jacksonObjectMapper()

    fun search(dto: SearchQueryDto): List<SearchDocument> {
        val conditions = mutableListOf("1=1")
        val params = mutableListOf<Any?>()

        if (!dto.query.isNullOrBlank()) {
            conditions.add("searchable_text @@ plainto_tsquery('simple', ?)")
            params.add(dto.query)
        }
        if (dto.brand != null) {
            conditions.add("brand = ?")
            params.add(dto.brand)
        }
        if (dto.categoryId != null) {
            conditions.add("category_id = ?")
            params.add(dto.categoryId)
        }
        if (dto.attributes.isNotEmpty()) {
            conditions.add("attributes @> ?::jsonb")
            params.add(mapper.writeValueAsString(dto.attributes))
        }
        // minPrice/maxPrice are not applicable: price data lives in pricing-service,
        // search_documents has no price column.

        val offset = dto.page * dto.size
        val sql = """
            SELECT product_id, sku, name, brand, category_id, localized_names, attributes, status, indexed_at
            FROM search_documents
            WHERE ${conditions.joinToString(" AND ")}
            LIMIT ? OFFSET ?
        """.trimIndent()
        params.add(dto.size)
        params.add(offset)

        return jdbc.query(sql, params.toTypedArray()) { rs, _ ->
            val localizedNamesJson = rs.getString("localized_names")
            val attributesJson = rs.getString("attributes")
            SearchDocument(
                productId = rs.getLong("product_id"),
                sku = rs.getString("sku"),
                name = rs.getString("name"),
                brand = rs.getString("brand"),
                categoryId = rs.getLong("category_id"),
                localizedNames = if (localizedNamesJson != null) mapper.readValue(localizedNamesJson) else emptyMap(),
                attributes = if (attributesJson != null) mapper.readValue(attributesJson) else emptyMap(),
                searchableText = "",
                status = rs.getString("status"),
                indexedAt = rs.getTimestamp("indexed_at").toInstant()
            )
        }
    }

    fun suggestions(prefix: String): List<String> {
        val sql = "SELECT DISTINCT name FROM search_documents WHERE name ILIKE ? LIMIT 10"
        return jdbc.queryForList(sql, String::class.java, "$prefix%")
    }

    fun indexProduct(dto: IndexProductDto) {
        val searchableText = listOf(dto.name, dto.brand, dto.sku).joinToString(" ")
        jdbc.update(
            """
            INSERT INTO search_documents (product_id, sku, name, brand, category_id, searchable_text, localized_names, attributes, status, indexed_at)
            VALUES (?, ?, ?, ?, ?, to_tsvector('simple', ?), ?::jsonb, ?::jsonb, ?, ?)
            ON CONFLICT (product_id) DO UPDATE
            SET sku = EXCLUDED.sku, name = EXCLUDED.name, brand = EXCLUDED.brand,
                category_id = EXCLUDED.category_id, searchable_text = EXCLUDED.searchable_text,
                localized_names = EXCLUDED.localized_names, attributes = EXCLUDED.attributes,
                status = EXCLUDED.status, indexed_at = EXCLUDED.indexed_at
            """.trimIndent(),
            dto.productId, dto.sku, dto.name, dto.brand, dto.categoryId,
            searchableText,
            mapper.writeValueAsString(dto.localizedNames),
            mapper.writeValueAsString(dto.attributes),
            dto.status, Instant.now()
        )
    }

    fun removeProduct(productId: Long) {
        jdbc.update("DELETE FROM search_documents WHERE product_id = ?", productId)
    }
}
