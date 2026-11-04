package com.rest.controller.product

import com.data.entity.Product
import com.data.entity.ProductStatus
import com.rest.controller.product.adapter.toDto
import com.rest.controller.product.data.CreateProductRequest
import com.rest.controller.product.data.UpdateProductRequest
import com.service.product.ProductService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {

    @GetMapping
    fun getAll(
        @RequestParam categoryId: Long?,
        @RequestParam status: ProductStatus?,
        pageable: Pageable
    ): Page<Product> = productService.findAll(categoryId, status, pageable)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Product = productService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateProductRequest): Product =
        productService.create(request.toDto())

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: UpdateProductRequest): Product =
        productService.update(id, request.toDto())

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun archive(@PathVariable id: Long) = productService.archive(id)
}