package com.rest.controller.product.data

import com.data.entity.ProductStatus
import java.time.Instant

data class ProductResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val brandId: Long,
    val categoryId: Long,
    val productTypeId: Long,
    val description: String?,
    val status: ProductStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)