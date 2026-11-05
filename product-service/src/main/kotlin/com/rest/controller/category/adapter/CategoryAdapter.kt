package com.rest.controller.category.adapter

import com.rest.controller.category.data.CreateCategoryRequest
import com.rest.controller.category.data.UpdateCategoryRequest
import com.service.category.dto.CreateCategoryDto
import com.service.category.dto.UpdateCategoryDto

fun CreateCategoryRequest.toDto() = CreateCategoryDto(parentId = parentId, name = name, slug = slug)
fun UpdateCategoryRequest.toDto() = UpdateCategoryDto(parentId = parentId, name = name, slug = slug)