package com.service.search

import com.data.entity.SearchDocument
import com.service.search.dto.IndexProductDto
import com.service.search.dto.SearchQueryDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class SearchService(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate
) {

    fun search(dto: SearchQueryDto): List<SearchDocument> {
        val conditions = mutableListOf("1=1")
        val params = mutableListOf<Any>()

        if (!dto.query.isNullOrBlank()) {
            conditions.add("searchable_text @@ plainto_tsquery('simple', ?)")
            params.add(dto.query)
        }
        if (dto.brand != null) {
            conditions.add("brand = ?")
            params.add(dto.brand)
        }
        if (dto.categoryId != null) {
            conditions.add("category_id = ?::uuid")
            params.add(dto.categoryId.toString())
        }

        val offset = dto.page * dto.size
        val sql = """
            SELECT product_id, sku, name, brand, category_id, status, indexed_at
            FROM search_documents
            WHERE ${conditions.joinToString(" AND ")}
            LIMIT ? OFFSET ?
        """.trimIndent()
        params.add(dto.size)
        params.add(offset)

        return jdbc.query(sql, params.toTypedArray()) { rs, _ ->
            SearchDocument(
                productId = UUID.fromString(rs.getString("product_id")),
                sku = rs.getString("sku"),
                name = rs.getString("name"),
                brand = rs.getString("brand"),
                categoryId = UUID.fromString(rs.getString("category_id")),
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
            INSERT INTO search_documents (product_id, sku, name, brand, category_id, searchable_text, status, indexed_at)
            VALUES (?::uuid, ?, ?, ?, ?::uuid, to_tsvector('simple', ?), ?, ?)
            ON CONFLICT (product_id) DO UPDATE
            SET sku = EXCLUDED.sku, name = EXCLUDED.name, brand = EXCLUDED.brand,
                category_id = EXCLUDED.category_id, searchable_text = EXCLUDED.searchable_text,
                status = EXCLUDED.status, indexed_at = EXCLUDED.indexed_at
            """.trimIndent(),
            dto.productId.toString(), dto.sku, dto.name, dto.brand, dto.categoryId.toString(),
            searchableText, dto.status, Instant.now()
        )
    }

    fun removeProduct(productId: UUID) {
        jdbc.update("DELETE FROM search_documents WHERE product_id = ?::uuid", productId.toString())
    }
}
