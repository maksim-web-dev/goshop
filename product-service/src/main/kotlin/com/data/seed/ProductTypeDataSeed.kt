package com.data.seed

import com.config.props.SeedProps
import com.data.entity.ProductType
import com.data.repository.CategoryRepository
import com.data.repository.ProductTypeRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
@Order(8)
class ProductTypeDataSeed(
    private val productTypeRepository: ProductTypeRepository,
    private val categoryRepository: CategoryRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (productTypeRepository.count() > seedProps.stopCount) return

        val categoriesBySlug = categoryRepository.findAll().associateBy { it.slug }

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_type.json")
        val dtos: Array<ProductTypeDto> = jacksonObjectMapper().readValue(json)

        val items = dtos.map { dto ->
            val category = categoriesBySlug[dto.categorySlug]
                ?: throw IllegalArgumentException("Category slug '${dto.categorySlug}' not found")
            ProductType(name = dto.name, category = category)
        }
        productTypeRepository.saveAll(items)
    }

    private data class ProductTypeDto(
        @param:JsonProperty("name")          val name: String,
        @param:JsonProperty("category_slug") val categorySlug: String
    )
}