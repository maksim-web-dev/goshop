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
@Order(5)
class ParameterGroupItemDataSeed(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM parameter_group_items", Long::class.java) ?: 0L
        if (count > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_parameter_group_item.json")
        val dtos: Array<ParameterGroupItemDto> = jacksonObjectMapper().readValue(json)
        dtos.forEach { dto ->
            jdbc.update(
                """
                INSERT INTO parameter_group_items (id, name, parameter_group_id)
                SELECT ?, ?, id FROM parameter_groups WHERE name = ?
                ON CONFLICT (id) DO NOTHING
                """.trimIndent(),
                dto.id, dto.name, dto.parameterGroupName
            )
        }
        jdbc.queryForObject(
            "SELECT setval(pg_get_serial_sequence('parameter_group_items', 'id'), COALESCE((SELECT MAX(id) FROM parameter_group_items), 1))",
            Long::class.java
        )
    }

    private data class ParameterGroupItemDto(
        @param:JsonProperty("id")                   val id: Long,
        @param:JsonProperty("name")                 val name: String,
        @param:JsonProperty("parameter_group_name") val parameterGroupName: String
    )
}
