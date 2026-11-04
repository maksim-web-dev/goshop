package com.data.seed

import com.config.props.SeedProps
import com.data.entity.Product
import com.data.entity.ProductStatus
import com.data.repository.BrandRepository
import com.data.repository.CategoryRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.data.repository.ProductRepository
import com.data.repository.ProductTypeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
@Order(9)
class ProductDataSeed(
    private val productRepository: ProductRepository,
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository,
    private val productTypeRepository: ProductTypeRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (productRepository.count() > seedProps.stopCount) return

        val brandsByName      = brandRepository.findAll().associateBy { it.name }
        val categoriesBySlug  = categoryRepository.findAll().associateBy { it.slug }
        val productTypes      = productTypeRepository.findAll()

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product.json")
        val dtos: Array<ProductDto> = jacksonObjectMapper().readValue(json)

        val items = dtos.map { dto ->
            val brand = brandsByName[dto.brandName]
                ?: throw IllegalArgumentException("Brand '${dto.brandName}' not found")
            val category = categoriesBySlug[dto.categorySlug]
                ?: throw IllegalArgumentException("Category slug '${dto.categorySlug}' not found")
            val productType = productTypes.firstOrNull {
                it.name == dto.productTypeName && it.category.slug == dto.categorySlug
            } ?: throw IllegalArgumentException("ProductType '${dto.productTypeName}' in '${dto.categorySlug}' not found")

            Product(
                sku = dto.sku,
                name = dto.name,
                brand = brand,
                category = category,
                productType = productType,
                description = dto.description,
                status = ProductStatus.valueOf(dto.status)
            )
        }
        productRepository.saveAll(items)
    }

    private data class ProductDto(
        @param:JsonProperty("sku")               val sku: String,
        @param:JsonProperty("name")              val name: String,
        @param:JsonProperty("brand_name")        val brandName: String,
        @param:JsonProperty("category_slug")     val categorySlug: String,
        @param:JsonProperty("product_type_name") val productTypeName: String,
        @param:JsonProperty("description")       val description: String? = null,
        @param:JsonProperty("status")            val status: String = "DRAFT"
    )
}