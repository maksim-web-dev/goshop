package com.data.seed

import com.config.props.SeedProps
import com.data.entity.CategoryParameter
import com.data.entity.ParameterType
import com.data.repository.CategoryParameterRepository
import com.data.repository.CategoryRepository
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
@Order(7)
class CategoryParameterDataSeed(
    private val categoryParameterRepository: CategoryParameterRepository,
    private val categoryRepository: CategoryRepository,
    private val parameterGroupRepository: ParameterGroupRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (categoryParameterRepository.count() > seedProps.stopCount) return

        val categoriesBySlug   = categoryRepository.findAll().associateBy { it.slug }
        val groupsByName       = parameterGroupRepository.findAll().associateBy { it.name }

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_category_parameter.json")
        val dtos: Array<CategoryParameterDto> = jacksonObjectMapper().readValue(json)

        val items = dtos.map { dto ->
            val category = categoriesBySlug[dto.categorySlug]
                ?: throw IllegalArgumentException("Category slug '${dto.categorySlug}' not found")
            val parameterGroup = dto.parameterGroupName?.let {
                groupsByName[it] ?: throw IllegalArgumentException("ParameterGroup '$it' not found")
            }
            CategoryParameter(
                name = dto.name,
                order = dto.sortOrder,
                category = category,
                parameterType = ParameterType.valueOf(dto.parameterType),
                parameterGroup = parameterGroup
            )
        }
        categoryParameterRepository.saveAll(items)
    }

    private data class CategoryParameterDto(
        @param:JsonProperty("name")                 val name: String,
        @param:JsonProperty("sort_order")           val sortOrder: Int,
        @param:JsonProperty("category_slug")        val categorySlug: String,
        @param:JsonProperty("parameter_type")       val parameterType: String,
        @param:JsonProperty("parameter_group_name") val parameterGroupName: String? = null
    )
}