package com.client.dto

import java.time.Instant

data class ProductResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val brandId: Long,
    val categoryId: Long,
    val productTypeId: Long,
    val description: String?,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ProductDetailsResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val status: String,
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

data class ProductBatchRequest(val productIds: List<Long>)

data class ProductBatchResponse(
    val products: List<ProductResponse>,
    val notFoundIds: List<Long>
)

data class CategoryResponse(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val slug: String
)
