package com.data.seed

import com.config.props.SeedProps
import com.data.entity.Component
import com.data.repository.ComponentGroupRepository
import com.data.repository.ComponentRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component as SpringComponent
import org.springframework.transaction.annotation.Transactional

@SpringComponent
@Transactional
@Order(6)
class ComponentDataSeed(
    private val componentRepository: ComponentRepository,
    private val componentGroupRepository: ComponentGroupRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (componentRepository.count() > seedProps.stopCount) return

        val groupsByName = componentGroupRepository.findAll().associateBy { it.name }

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_component.json")
        val dtos: Array<ComponentDto> = jacksonObjectMapper().readValue(json)

        val items = dtos.map { dto ->
            val group = groupsByName[dto.componentGroupName]
                ?: throw IllegalArgumentException("ComponentGroup '${dto.componentGroupName}' not found")
            Component(name = dto.name, componentGroup = group)
        }
        componentRepository.saveAll(items)
    }

    private data class ComponentDto(
        @param:JsonProperty("name")                 val name: String,
        @param:JsonProperty("component_group_name") val componentGroupName: String
    )
}