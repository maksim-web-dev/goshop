package com.rest.controller.category.data

data class CategoryCreateRequest(
    val parentId: Long? = null,
    val name: String,
    val slug: String
)