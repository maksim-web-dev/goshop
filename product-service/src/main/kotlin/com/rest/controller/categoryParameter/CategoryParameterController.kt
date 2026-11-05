package com.rest.controller.categoryParameter

import com.rest.controller.categoryParameter.adapter.CategoryParameterCreateDtoAdapter
import com.rest.controller.categoryParameter.adapter.CategoryParameterResponseAdapter
import com.rest.controller.categoryParameter.adapter.CategoryParameterUpdateDtoAdapter
import com.rest.controller.categoryParameter.data.CategoryParameterCreateRequest
import com.rest.controller.categoryParameter.data.CategoryParameterResponse
import com.rest.controller.categoryParameter.data.CategoryParameterUpdateRequest
import com.service.categoryParameter.CategoryParameterService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/category-parameters")
class CategoryParameterController(private val categoryParameterService: CategoryParameterService) {

    @GetMapping
    fun getByCategory(@RequestParam categoryId: Long): List<CategoryParameterResponse> =
        with(CategoryParameterResponseAdapter) { categoryParameterService.findByCategoryId(categoryId).toApiModel() }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): CategoryParameterResponse =
        with(CategoryParameterResponseAdapter) { categoryParameterService.findById(id).toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody payload: CategoryParameterCreateRequest): CategoryParameterResponse {
        val dto = with(CategoryParameterCreateDtoAdapter) { payload.toServiceModel() }
        return with(CategoryParameterResponseAdapter) { categoryParameterService.create(dto).toApiModel() }
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: CategoryParameterUpdateRequest): CategoryParameterResponse {
        val dto = with(CategoryParameterUpdateDtoAdapter) { request.toServiceModel() }
        return with(CategoryParameterResponseAdapter) { categoryParameterService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = categoryParameterService.delete(id)
}