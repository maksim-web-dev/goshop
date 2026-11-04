package com.rest.controller.category.data

data class CreateCategoryRequest(
    val parentId: Long? = null,
    val name: String,
    val slug: String
)