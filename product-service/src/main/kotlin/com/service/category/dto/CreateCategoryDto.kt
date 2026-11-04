package com.service.category.dto

data class CreateCategoryDto(
    val parentId: Long? = null,
    val name: String,
    val slug: String
)