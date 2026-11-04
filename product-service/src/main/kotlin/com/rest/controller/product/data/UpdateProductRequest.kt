package com.rest.controller.product.data

import com.data.entity.ProductStatus

data class UpdateProductRequest(
    val name: String? = null,
    val brandId: Long? = null,
    val categoryId: Long? = null,
    val productTypeId: Long? = null,
    val description: String? = null,
    val status: ProductStatus? = null
)