package com.rest.controller.category

import com.rest.controller.category.adapter.CategoryCreateDtoAdapter
import com.rest.controller.category.adapter.CategoryResponseAdapter
import com.rest.controller.category.adapter.CategoryUpdateDtoAdapter
import com.rest.controller.category.data.CategoryCreateRequest
import com.rest.controller.category.data.CategoryResponse
import com.rest.controller.category.data.CategoryUpdateRequest
import com.service.category.CategoryService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(private val categoryService: CategoryService) {

    @GetMapping
    fun getAll(): List<CategoryResponse> =
        with(CategoryResponseAdapter) { categoryService.findAll().toApiModel() }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): CategoryResponse =
        with(CategoryResponseAdapter) { categoryService.findById(id).toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody payload: CategoryCreateRequest): CategoryResponse {
        val dto = with(CategoryCreateDtoAdapter) { payload.toServiceModel() }
        return with(CategoryResponseAdapter) { categoryService.create(dto).toApiModel() }
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: CategoryUpdateRequest): CategoryResponse {
        val dto = with(CategoryUpdateDtoAdapter) { request.toServiceModel() }
        return with(CategoryResponseAdapter) { categoryService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = categoryService.delete(id)
}