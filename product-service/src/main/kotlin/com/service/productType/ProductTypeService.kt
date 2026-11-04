package com.service.productType

import com.data.entity.CategoryParameter
import com.data.entity.ProductType
import com.data.repository.CategoryParameterRepository
import com.data.repository.CategoryRepository
import com.data.repository.ProductTypeRepository
import com.service.productType.dto.CreateProductTypeDto
import com.service.productType.dto.UpdateProductTypeDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ProductTypeService(
    private val productTypeRepository: ProductTypeRepository,
    private val categoryRepository: CategoryRepository,
    private val categoryParameterRepository: CategoryParameterRepository
) {

    fun findAll(): List<ProductType> = productTypeRepository.findAll()

    fun findById(id: Long): ProductType =
        productTypeRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "ProductType $id not found") }

    fun findAttributeSchema(productTypeId: Long): List<CategoryParameter> {
        val productType = findById(productTypeId)
        return categoryParameterRepository.findByCategoryId(productType.category.id)
    }

    @Transactional
    fun create(dto: CreateProductTypeDto): ProductType {
        val category = categoryRepository.findById(dto.categoryId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Category ${dto.categoryId} not found") }
        return productTypeRepository.save(ProductType(category = category, name = dto.name))
    }

    @Transactional
    fun update(id: Long, dto: UpdateProductTypeDto): ProductType {
        val existing = findById(id)
        val category = dto.categoryId?.let {
            categoryRepository.findById(it)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Category $it not found") }
        } ?: existing.category
        return productTypeRepository.save(
            ProductType(id = existing.id, category = category, name = dto.name ?: existing.name)
        )
    }

    @Transactional
    fun delete(id: Long) {
        if (!productTypeRepository.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "ProductType $id not found")
        productTypeRepository.deleteById(id)
    }
}