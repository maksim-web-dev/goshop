package com.rest.controller.productAttribute

import com.rest.controller.productAttribute.adapter.ProductAttributeResponseAdapter
import com.rest.controller.productAttribute.adapter.ProductAttributeSetDtoAdapter
import com.rest.controller.productAttribute.data.ProductAttributeResponse
import com.rest.controller.productAttribute.data.ProductAttributeSetRequest
import com.service.productAttribute.ProductAttributeService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products/{productId}/attributes")
class ProductAttributeController(private val productAttributeService: ProductAttributeService) {

    @GetMapping
    fun getByProduct(@PathVariable productId: Long): List<ProductAttributeResponse> =
        with(ProductAttributeResponseAdapter) { productAttributeService.findByProductId(productId).toApiModel() }

    @PutMapping
    fun setAttributes(
        @PathVariable productId: Long,
        @RequestBody payload: ProductAttributeSetRequest
    ) {
        val dto = with(ProductAttributeSetDtoAdapter) { payload.toServiceModel() }
        productAttributeService.setProductAttributes(productId, dto)
    }
}