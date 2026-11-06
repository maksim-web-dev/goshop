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
@Order(7)
class CategoryParameterDataSeed(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM category_parameters", Long::class.java) ?: 0L
        if (count > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_category_parameter.json")
        val dtos: Array<CategoryParameterDto> = jacksonObjectMapper().readValue(json)
        dtos.forEach { dto ->
            jdbc.update(
                "INSERT INTO category_parameters (id, name, code, sort_order, category_id, parameter_type) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                dto.id, dto.name, dto.code, dto.sortOrder, dto.categoryId, dto.parameterType
            )
        }
        jdbc.queryForObject(
            "SELECT setval(pg_get_serial_sequence('category_parameters', 'id'), COALESCE((SELECT MAX(id) FROM category_parameters), 1))",
            Long::class.java
        )
    }

    private data class CategoryParameterDto(
        @param:JsonProperty("id")             val id: Long,
        @param:JsonProperty("name")           val name: String,
        @param:JsonProperty("code")           val code: String? = null,
        @param:JsonProperty("sort_order")     val sortOrder: Int,
        @param:JsonProperty("category_id")    val categoryId: Long,
        @param:JsonProperty("parameter_type") val parameterType: String
    )
}
