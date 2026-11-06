package com.service.product

import com.data.entity.Product
import com.data.entity.ProductStatus
import com.data.repository.BrandRepository
import com.data.repository.CategoryRepository
import com.data.repository.ProductRepository
import com.data.repository.ProductTypeRepository
import com.service.product.dto.CreateProductDto
import com.service.product.dto.UpdateProductDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository,
    private val productTypeRepository: ProductTypeRepository
) {

    fun findAll(categoryId: Long?, status: ProductStatus?, pageable: Pageable): Page<Product> =
        productRepository.findAll(buildSpec(categoryId, status), pageable)

    fun findById(id: Long): Product =
        productRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Product $id not found") }

    @Transactional
    fun create(dto: CreateProductDto): Product {
        if (productRepository.existsBySku(dto.sku))
            throw ResponseStatusException(HttpStatus.CONFLICT, "Product with SKU '${dto.sku}' already exists")
        val brand = brandRepository.findById(dto.brandId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Brand ${dto.brandId} not found") }
        val category = categoryRepository.findById(dto.categoryId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Category ${dto.categoryId} not found") }
        val productType = productTypeRepository.findById(dto.productTypeId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "ProductType ${dto.productTypeId} not found") }
        return productRepository.save(
            Product(
                sku = dto.sku,
                name = dto.name,
                brand = brand,
                category = category,
                productType = productType,
                description = dto.description
            )
        )
    }

    @Transactional
    fun update(id: Long, dto: UpdateProductDto): Product {
        val existing = findById(id)
        val brand = dto.brandId?.let {
            brandRepository.findById(it)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Brand $it not found") }
        } ?: existing.brand
        val category = dto.categoryId?.let {
            categoryRepository.findById(it)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Category $it not found") }
        } ?: existing.category
        val productType = dto.productTypeId?.let {
            productTypeRepository.findById(it)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "ProductType $it not found") }
        } ?: existing.productType
        return productRepository.save(
            Product(
                id = existing.id,
                sku = existing.sku,
                name = dto.name ?: existing.name,
                brand = brand,
                category = category,
                productType = productType,
                description = dto.description ?: existing.description,
                status = dto.status ?: existing.status,
                createdAt = existing.createdAt,
                updatedAt = Instant.now()
            )
        )
    }

    @Transactional
    fun archive(id: Long) {
        val existing = findById(id)
        productRepository.save(
            Product(
                id = existing.id,
                sku = existing.sku,
                name = existing.name,
                brand = existing.brand,
                category = existing.category,
                productType = existing.productType,
                description = existing.description,
                status = ProductStatus.ARCHIVED,
                createdAt = existing.createdAt,
                updatedAt = Instant.now()
            )
        )
    }

    private fun buildSpec(categoryId: Long?, status: ProductStatus?): Specification<Product> {
        var spec = Specification.where<Product>(null)
        if (categoryId != null) {
            spec = spec.and { root, _, cb -> cb.equal(root.get<Any>("category").get<Long>("id"), categoryId) }
        }
        if (status != null) {
            spec = spec.and { root, _, cb -> cb.equal(root.get<ProductStatus>("status"), status) }
        }
        return spec
    }
}