package com.service.categoryParameter

import com.data.entity.CategoryParameter
import com.data.repository.CategoryParameterRepository
import com.data.repository.CategoryRepository
import com.data.repository.ParameterGroupRepository
import com.service.categoryParameter.dto.CreateCategoryParameterDto
import com.service.categoryParameter.dto.UpdateCategoryParameterDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CategoryParameterService(
    private val categoryParameterRepository: CategoryParameterRepository,
    private val categoryRepository: CategoryRepository,
    private val parameterGroupRepository: ParameterGroupRepository
) {

    fun findByCategoryId(categoryId: Long): List<CategoryParameter> =
        categoryParameterRepository.findByCategoryId(categoryId)

    fun findById(id: Long): CategoryParameter =
        categoryParameterRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "CategoryParameter $id not found") }

    @Transactional
    fun create(dto: CreateCategoryParameterDto): CategoryParameter {
        val category = categoryRepository.findById(dto.categoryId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Category ${dto.categoryId} not found") }
        val parameterGroup = dto.parameterGroupId?.let {
            parameterGroupRepository.findById(it)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "ParameterGroup $it not found") }
        }
        return categoryParameterRepository.save(
            CategoryParameter(
                name = dto.name,
                order = dto.order,
                category = category,
                parameterType = dto.parameterType,
                intValue = dto.intValue,
                doubleValue2 = dto.doubleValue2,
                doubleValue3 = dto.doubleValue3,
                booleanValue = dto.booleanValue,
                stringValue = dto.stringValue,
                parameterGroup = parameterGroup
            )
        )
    }

    @Transactional
    fun update(id: Long, dto: UpdateCategoryParameterDto): CategoryParameter {
        val existing = findById(id)
        val parameterGroup = dto.parameterGroupId?.let {
            parameterGroupRepository.findById(it)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "ParameterGroup $it not found") }
        } ?: existing.parameterGroup
        return categoryParameterRepository.save(
            CategoryParameter(
                id = existing.id,
                name = dto.name ?: existing.name,
                order = dto.order ?: existing.order,
                category = existing.category,
                parameterType = dto.parameterType ?: existing.parameterType,
                intValue = dto.intValue ?: existing.intValue,
                doubleValue2 = dto.doubleValue2 ?: existing.doubleValue2,
                doubleValue3 = dto.doubleValue3 ?: existing.doubleValue3,
                booleanValue = dto.booleanValue ?: existing.booleanValue,
                stringValue = dto.stringValue ?: existing.stringValue,
                parameterGroup = parameterGroup
            )
        )
    }

    @Transactional
    fun delete(id: Long) {
        if (!categoryParameterRepository.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "CategoryParameter $id not found")
        categoryParameterRepository.deleteById(id)
    }
}