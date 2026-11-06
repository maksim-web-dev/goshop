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
@Order(10)
class ProductAttributeDataSeed(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM product_attributes", Long::class.java) ?: 0L
        if (count > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_product_attribute.json")
        val dtos: Array<ProductAttributeDto> = jacksonObjectMapper().readValue(json)
        dtos.forEach { dto ->
            jdbc.update(
                "INSERT INTO product_attributes (product_id, category_parameter_id, value) VALUES (?, ?, ?) ON CONFLICT (product_id, category_parameter_id) DO NOTHING",
                dto.productId, dto.categoryParameterId, dto.value
            )
        }
    }

    private data class ProductAttributeDto(
        @param:JsonProperty("product_id")            val productId: Long,
        @param:JsonProperty("category_parameter_id") val categoryParameterId: Long,
        @param:JsonProperty("value")                 val value: String
    )
}
