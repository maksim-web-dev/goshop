package com.rest.controller.category.adapter

import com.rest.controller.category.data.CategoryUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.category.dto.UpdateCategoryDto

object CategoryUpdateDtoAdapter : ApiTypeAdapter<CategoryUpdateRequest, UpdateCategoryDto>() {

    override fun CategoryUpdateRequest.toServiceModel() = UpdateCategoryDto(
        parentId = parentId,
        name = name,
        slug = slug
    )
}