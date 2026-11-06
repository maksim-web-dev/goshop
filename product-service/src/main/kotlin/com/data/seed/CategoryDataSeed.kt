package com.data.seed

import com.config.props.SeedProps
import com.data.entity.Category
import com.data.repository.CategoryRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.get

@Component
@Transactional
@Order(4)
class CategoryDataSeed(
    private val categoryRepository: CategoryRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (categoryRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_category.json")
        val dtos: Array<CategoryDto> = jacksonObjectMapper().readValue(json)

        val savedBySlug = mutableMapOf<String, Category>()

        // first pass: root categories (no parent)
        categoryRepository.saveAll(
            dtos.filter { it.parentSlug == null }
                .map { Category(name = it.name, slug = it.slug) }
        ).forEach { savedBySlug[it.slug] = it }

        // second pass: child categories
        val children = dtos.filter { it.parentSlug != null }.map { dto ->
            val parent = savedBySlug[dto.parentSlug]
                ?: throw IllegalArgumentException("Parent slug '${dto.parentSlug}' not found for '${dto.slug}'")
            Category(name = dto.name, slug = dto.slug, parent = parent)
        }
        if (children.isNotEmpty()) {
            categoryRepository.saveAll(children)
        }
    }

    private data class CategoryDto(
        @param:JsonProperty("name")        val name: String,
        @param:JsonProperty("slug")        val slug: String,
        @param:JsonProperty("parent_slug") val parentSlug: String? = null
    )
}