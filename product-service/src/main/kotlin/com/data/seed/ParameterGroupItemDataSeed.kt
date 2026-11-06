package com.data.seed

import com.config.props.SeedProps
import com.data.entity.ParameterGroupItem
import com.data.repository.ParameterGroupItemRepository
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
@Order(5)
class ParameterGroupItemDataSeed(
    private val parameterGroupItemRepository: ParameterGroupItemRepository,
    private val parameterGroupRepository: ParameterGroupRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (parameterGroupItemRepository.count() > seedProps.stopCount) return

        val groupsByName = parameterGroupRepository.findAll().associateBy { it.name }

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_parameter_group_item.json")
        val dtos: Array<ParameterGroupItemDto> = jacksonObjectMapper().readValue(json)

        val items = dtos.map { dto ->
            val group = groupsByName[dto.parameterGroupName]
                ?: throw IllegalArgumentException("ParameterGroup '${dto.parameterGroupName}' not found")
            ParameterGroupItem(name = dto.name, parameterGroup = group)
        }
        parameterGroupItemRepository.saveAll(items)
    }

    private data class ParameterGroupItemDto(
        @param:JsonProperty("name")                 val name: String,
        @param:JsonProperty("parameter_group_name") val parameterGroupName: String
    )
}