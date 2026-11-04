package com.service.category

import com.data.entity.Category
import com.data.repository.CategoryRepository
import com.service.category.dto.CreateCategoryDto
import com.service.category.dto.UpdateCategoryDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CategoryService(private val categoryRepository: CategoryRepository) {

    fun findAll(): List<Category> = categoryRepository.findAll()

    fun findById(id: Long): Category =
        categoryRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Category $id not found") }

    @Transactional
    fun create(dto: CreateCategoryDto): Category {
        val parent = dto.parentId?.let { findById(it) }
        return categoryRepository.save(Category(parent = parent, name = dto.name, slug = dto.slug))
    }

    @Transactional
    fun update(id: Long, dto: UpdateCategoryDto): Category {
        val existing = findById(id)
        val parent = dto.parentId?.let { findById(it) } ?: existing.parent
        return categoryRepository.save(
            Category(
                id = existing.id,
                parent = parent,
                name = dto.name ?: existing.name,
                slug = dto.slug ?: existing.slug
            )
        )
    }

    @Transactional
    fun delete(id: Long) {
        if (!categoryRepository.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Category $id not found")
        categoryRepository.deleteById(id)
    }
}