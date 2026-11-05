package com.rest.controller.product

import com.data.entity.ProductStatus
import com.rest.controller.product.adapter.ProductCreateDtoAdapter
import com.rest.controller.product.adapter.ProductResponseAdapter
import com.rest.controller.product.adapter.ProductUpdateDtoAdapter
import com.rest.controller.product.data.ProductCreateRequest
import com.rest.controller.product.data.ProductResponse
import com.rest.controller.product.data.ProductUpdateRequest
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
    ): Page<ProductResponse> =
        with(ProductResponseAdapter) { productService.findAll(categoryId, status, pageable).map { it.toApiModel() } }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ProductResponse =
        with(ProductResponseAdapter) { productService.findById(id).toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody payload: ProductCreateRequest): ProductResponse {
        val dto = with(ProductCreateDtoAdapter) { payload.toServiceModel() }
        return with(ProductResponseAdapter) { productService.create(dto).toApiModel() }
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: ProductUpdateRequest): ProductResponse {
        val dto = with(ProductUpdateDtoAdapter) { request.toServiceModel() }
        return with(ProductResponseAdapter) { productService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun archive(@PathVariable id: Long) = productService.archive(id)
}