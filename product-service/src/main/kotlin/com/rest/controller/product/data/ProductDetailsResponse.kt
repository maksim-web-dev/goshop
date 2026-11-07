package com.rest.controller.product.data

import com.data.entity.ProductStatus
import java.time.Instant

data class ProductDetailsResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val status: ProductStatus,
    val brand: BrandInfo,
    val category: CategoryInfo,
    val productType: ProductTypeInfo,
    val attributes: List<AttributeInfo>,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    data class BrandInfo(val id: Long, val name: String, val websiteUrl: String?)
    data class CategoryInfo(val id: Long, val name: String, val slug: String)
    data class ProductTypeInfo(val id: Long, val name: String)
    data class AttributeInfo(val code: String?, val name: String, val value: String)
}
