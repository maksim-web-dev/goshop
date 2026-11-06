package com.data.repository

import com.data.entity.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long> {
    fun existsByParentId(parentId: Long): Boolean
}