package com.rest.controller.category.adapter

import com.data.entity.Category
import com.rest.controller.category.data.CategoryResponse
import com.rest.util.ApiTypeAdapter

object CategoryResponseAdapter : ApiTypeAdapter<CategoryResponse, Category>() {

    override fun Category.toApiModel() = CategoryResponse(
        id = id,
        parentId = parent?.id,
        name = name,
        slug = slug
    )
}