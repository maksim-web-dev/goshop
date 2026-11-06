package com.data.seed

import com.config.props.SeedProps
import com.data.entity.ComponentGroup
import com.data.repository.ComponentGroupRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
@Order(2)
class ComponentGroupDataSeed(
    private val componentGroupRepository: ComponentGroupRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (componentGroupRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_component_group.json")
        val dtos: Array<ComponentGroupDto> = jacksonObjectMapper().readValue(json)
        componentGroupRepository.saveAll(dtos.map { ComponentGroup(name = it.name) })
    }

    private data class ComponentGroupDto(
        @param:JsonProperty("name") val name: String
    )
}