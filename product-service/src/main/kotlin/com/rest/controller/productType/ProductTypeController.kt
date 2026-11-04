package com.rest.controller.productType

import com.data.entity.CategoryParameter
import com.data.entity.ProductType
import com.rest.controller.productType.adapter.toDto
import com.rest.controller.productType.data.CreateProductTypeRequest
import com.rest.controller.productType.data.UpdateProductTypeRequest
import com.service.productType.ProductTypeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/product-types")
class ProductTypeController(private val productTypeService: ProductTypeService) {

    @GetMapping
    fun getAll(): List<ProductType> = productTypeService.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ProductType = productTypeService.findById(id)

    @GetMapping("/{id}/attributes")
    fun getAttributeSchema(@PathVariable id: Long): List<CategoryParameter> =
        productTypeService.findAttributeSchema(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateProductTypeRequest): ProductType =
        productTypeService.create(request.toDto())

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: UpdateProductTypeRequest): ProductType =
        productTypeService.update(id, request.toDto())

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = productTypeService.delete(id)
}