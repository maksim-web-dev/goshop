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
@Order(4)
class CategoryDataSeed(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM categories", Long::class.java) ?: 0L
        if (count > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_category.json")
        val dtos: Array<CategoryDto> = jacksonObjectMapper().readValue(json)

        // first pass: root categories (no parent)
        dtos.filter { it.parentId == null }.forEach { dto ->
            jdbc.update(
                "INSERT INTO categories (id, name, slug, parent_id) VALUES (?, ?, ?, NULL) ON CONFLICT (id) DO NOTHING",
                dto.id, dto.name, dto.slug
            )
        }
        // second pass: child categories
        dtos.filter { it.parentId != null }.forEach { dto ->
            jdbc.update(
                "INSERT INTO categories (id, name, slug, parent_id) VALUES (?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                dto.id, dto.name, dto.slug, dto.parentId
            )
        }
        jdbc.queryForObject(
            "SELECT setval(pg_get_serial_sequence('categories', 'id'), COALESCE((SELECT MAX(id) FROM categories), 1))",
            Long::class.java
        )
    }

    private data class CategoryDto(
        @param:JsonProperty("id")          val id: Long,
        @param:JsonProperty("name")        val name: String,
        @param:JsonProperty("slug")        val slug: String,
        @param:JsonProperty("parent_id")   val parentId: Long? = null
    )
}
