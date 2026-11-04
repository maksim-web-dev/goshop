package com.rest.controller.productAttribute

import com.data.entity.ProductAttribute
import com.rest.controller.productAttribute.adapter.toDto
import com.rest.controller.productAttribute.data.SetAttributesRequest
import com.service.productAttribute.ProductAttributeService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products/{productId}/attributes")
class ProductAttributeController(private val productAttributeService: ProductAttributeService) {

    @GetMapping
    fun getByProduct(@PathVariable productId: Long): List<ProductAttribute> =
        productAttributeService.findByProductId(productId)

    @PutMapping
    fun setAttributes(
        @PathVariable productId: Long,
        @RequestBody request: SetAttributesRequest
    ) = productAttributeService.setProductAttributes(productId, request.toDto())
}