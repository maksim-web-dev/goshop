package com.data.repository

import com.data.entity.CategoryParameter
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryParameterRepository : JpaRepository<CategoryParameter, Long> {
    fun findByCategoryId(categoryId: Long): List<CategoryParameter>
}