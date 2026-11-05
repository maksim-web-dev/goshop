package com.rest.controller.category.data

data class CategoryResponse(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val slug: String
)