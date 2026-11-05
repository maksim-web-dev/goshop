package com.rest.controller.productType

import com.rest.controller.categoryParameter.adapter.CategoryParameterResponseAdapter
import com.rest.controller.categoryParameter.data.CategoryParameterResponse
import com.rest.controller.productType.adapter.ProductTypeCreateDtoAdapter
import com.rest.controller.productType.adapter.ProductTypeResponseAdapter
import com.rest.controller.productType.adapter.ProductTypeUpdateDtoAdapter
import com.rest.controller.productType.data.ProductTypeCreateRequest
import com.rest.controller.productType.data.ProductTypeResponse
import com.rest.controller.productType.data.ProductTypeUpdateRequest
import com.service.productType.ProductTypeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/product-types")
class ProductTypeController(private val productTypeService: ProductTypeService) {

    @GetMapping
    fun getAll(): List<ProductTypeResponse> =
        with(ProductTypeResponseAdapter) { productTypeService.findAll().toApiModel() }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ProductTypeResponse =
        with(ProductTypeResponseAdapter) { productTypeService.findById(id).toApiModel() }

    @GetMapping("/{id}/attributes")
    fun getAttributeSchema(@PathVariable id: Long): List<CategoryParameterResponse> =
        with(CategoryParameterResponseAdapter) { productTypeService.findAttributeSchema(id).toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody payload: ProductTypeCreateRequest): ProductTypeResponse {
        val dto = with(ProductTypeCreateDtoAdapter) { payload.toServiceModel() }
        return with(ProductTypeResponseAdapter) { productTypeService.create(dto).toApiModel() }
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: ProductTypeUpdateRequest): ProductTypeResponse {
        val dto = with(ProductTypeUpdateDtoAdapter) { request.toServiceModel() }
        return with(ProductTypeResponseAdapter) { productTypeService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = productTypeService.delete(id)
}