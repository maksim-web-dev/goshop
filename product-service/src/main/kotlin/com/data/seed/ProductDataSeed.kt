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
@Order(9)
class ProductDataSeed(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM products", Long::class.java) ?: 0L
        if (count > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_product.json")
        val dtos: Array<ProductDto> = jacksonObjectMapper().readValue(json)
        val now = Instant.now()
        dtos.forEach { dto ->
            jdbc.update(
                """
                INSERT INTO products (id, sku, name, brand_id, category_id, product_type_id, description, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """.trimIndent(),
                dto.id, dto.sku, dto.name, dto.brandId, dto.categoryId, dto.productTypeId,
                dto.description, dto.status, now, now
            )
        }
        jdbc.queryForObject(
            "SELECT setval(pg_get_serial_sequence('products', 'id'), COALESCE((SELECT MAX(id) FROM products), 1))",
            Long::class.java
        )
    }

    private data class ProductDto(
        @param:JsonProperty("id")              val id: Long,
        @param:JsonProperty("sku")             val sku: String,
        @param:JsonProperty("name")            val name: String,
        @param:JsonProperty("brand_id")        val brandId: Long,
        @param:JsonProperty("category_id")     val categoryId: Long,
        @param:JsonProperty("product_type_id") val productTypeId: Long,
        @param:JsonProperty("description")     val description: String? = null,
        @param:JsonProperty("status")          val status: String = "DRAFT"
    )
}
