package com.service.product.dto

import com.data.entity.Product
import com.data.entity.ProductAttribute

data class ProductWithAttributesDto(
    val product: Product,
    val attributes: List<ProductAttribute>
)
