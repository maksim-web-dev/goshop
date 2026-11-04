package com.rest.controller.categoryParameter

import com.data.entity.CategoryParameter
import com.rest.controller.categoryParameter.adapter.toDto
import com.rest.controller.categoryParameter.data.CreateCategoryParameterRequest
import com.rest.controller.categoryParameter.data.UpdateCategoryParameterRequest
import com.service.categoryParameter.CategoryParameterService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/category-parameters")
class CategoryParameterController(private val categoryParameterService: CategoryParameterService) {

    @GetMapping
    fun getByCategory(@RequestParam categoryId: Long): List<CategoryParameter> =
        categoryParameterService.findByCategoryId(categoryId)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): CategoryParameter =
        categoryParameterService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateCategoryParameterRequest): CategoryParameter =
        categoryParameterService.create(request.toDto())

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: UpdateCategoryParameterRequest): CategoryParameter =
        categoryParameterService.update(id, request.toDto())

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = categoryParameterService.delete(id)
}