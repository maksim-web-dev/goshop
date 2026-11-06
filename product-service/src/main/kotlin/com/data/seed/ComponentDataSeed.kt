package com.data.seed

import com.config.props.SeedProps
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component as SpringComponent
import org.springframework.transaction.annotation.Transactional

@SpringComponent
@Transactional
@Order(6)
class ComponentDataSeed(
    @Qualifier("pgJdbc") private val jdbc: JdbcTemplate,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        val count = jdbc.queryForObject("SELECT COUNT(*) FROM components", Long::class.java) ?: 0L
        if (count > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_component.json")
        val dtos: Array<ComponentDto> = jacksonObjectMapper().readValue(json)
        dtos.forEach { dto ->
            jdbc.update(
                """
                INSERT INTO components (id, name, component_group_id)
                SELECT ?, ?, id FROM component_groups WHERE name = ?
                ON CONFLICT (id) DO NOTHING
                """.trimIndent(),
                dto.id, dto.name, dto.componentGroupName
            )
        }
        jdbc.queryForObject(
            "SELECT setval(pg_get_serial_sequence('components', 'id'), COALESCE((SELECT MAX(id) FROM components), 1))",
            Long::class.java
        )
    }

    private data class ComponentDto(
        @param:JsonProperty("id")                   val id: Long,
        @param:JsonProperty("name")                 val name: String,
        @param:JsonProperty("component_group_name") val componentGroupName: String
    )
}
