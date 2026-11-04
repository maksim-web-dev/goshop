package com.data.seed

import com.config.props.SeedProps
import com.data.entity.ParameterGroup
import com.data.repository.ParameterGroupRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
@Order(1)
class ParameterGroupDataSeed(
    private val parameterGroupRepository: ParameterGroupRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (parameterGroupRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/parameter_group.json")
        val dtos: Array<ParameterGroupDto> = jacksonObjectMapper().readValue(json)
        parameterGroupRepository.saveAll(dtos.map { ParameterGroup(name = it.name) })
    }

    private data class ParameterGroupDto(
        @param:JsonProperty("name") val name: String
    )
}