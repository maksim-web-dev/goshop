package com.rest.controller.category.adapter

import com.rest.controller.category.data.CategoryCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.category.dto.CreateCategoryDto

object CategoryCreateDtoAdapter : ApiTypeAdapter<CategoryCreateRequest, CreateCategoryDto>() {

    override fun CategoryCreateRequest.toServiceModel() = CreateCategoryDto(
        parentId = parentId,
        name = name,
        slug = slug
    )
}