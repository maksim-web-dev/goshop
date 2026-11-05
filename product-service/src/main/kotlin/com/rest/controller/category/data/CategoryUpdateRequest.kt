package com.rest.controller.category.data

data class CategoryUpdateRequest(
    val parentId: Long? = null,
    val name: String? = null,
    val slug: String? = null
)