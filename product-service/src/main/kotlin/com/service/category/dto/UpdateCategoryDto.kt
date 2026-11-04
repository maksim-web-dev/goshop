package com.service.category.dto

data class UpdateCategoryDto(
    val parentId: Long? = null,
    val name: String? = null,
    val slug: String? = null
)