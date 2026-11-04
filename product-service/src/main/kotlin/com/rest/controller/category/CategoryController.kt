package com.rest.controller.category

import com.data.entity.Category
import com.rest.controller.category.adapter.toDto
import com.rest.controller.category.data.CreateCategoryRequest
import com.rest.controller.category.data.UpdateCategoryRequest
import com.service.category.CategoryService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(private val categoryService: CategoryService) {

    @GetMapping
    fun getAll(): List<Category> = categoryService.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Category = categoryService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateCategoryRequest): Category =
        categoryService.create(request.toDto())

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: UpdateCategoryRequest): Category =
        categoryService.update(id, request.toDto())

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = categoryService.delete(id)
}