package com.rest.catalog.data

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant

// ── Product Detail ────────────────────────────────────────────────────────────

data class ProductDetailResponse(
    val product: ProductInfo,
    val price: PriceInfo?,
    val recommendations: RecommendationsInfo?,
    val warnings: List<String>
) {
    data class ProductInfo(
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
    )
    data class BrandInfo(val id: Long, val name: String, val websiteUrl: String?)
    data class CategoryInfo(val id: Long, val name: String, val slug: String)
    data class ProductTypeInfo(val id: Long, val name: String)
    data class AttributeInfo(val code: String?, val name: String, val value: String, val translatedName: String? = null)
    data class PriceInfo(val amount: BigDecimal, val originalAmount: BigDecimal, val currencyCode: String, val discountApplied: Boolean)
    data class RecommendationsInfo(val similar: List<Long>, val accessories: List<Long>)
}

// ── Batch Products ────────────────────────────────────────────────────────────

data class BatchProductsRequest(
    @field:NotEmpty(message = "productIds must not be empty")
    @field:Size(max = 100, message = "productIds must not exceed 100 items")
    val productIds: List<Long> = emptyList(),
    val lang: String? = null,
    val includePrices: Boolean = false
)

data class BatchProductItem(
    val id: Long,
    val sku: String,
    val name: String,
    val translatedName: String?,
    val description: String?,
    val status: String,
    val brandId: Long,
    val categoryId: Long,
    val price: ProductDetailResponse.PriceInfo?
)

data class BatchProductsResponse(
    val products: List<BatchProductItem>,
    val notFoundIds: List<Long>
)

// ── Search ────────────────────────────────────────────────────────────────────

data class CatalogSearchResponse(
    val results: List<SearchItem>,
    val page: Int,
    val size: Int
) {
    data class SearchItem(
        val productId: Long,
        val sku: String,
        val name: String,
        val translatedName: String?,
        val brand: String,
        val categoryId: Long,
        val status: String
    )
}

// ── Recommendations ───────────────────────────────────────────────────────────

data class RecommendedProductItem(
    val id: Long,
    val sku: String,
    val name: String,
    val translatedName: String?,
    val brandId: Long,
    val categoryId: Long,
    val status: String,
    val price: ProductDetailResponse.PriceInfo?
)

data class RecommendationsResponse(val items: List<RecommendedProductItem>)

// ── Compatibility ─────────────────────────────────────────────────────────────

data class CompatibilityRequest(
    @field:NotEmpty(message = "productIds must not be empty")
    @field:Size(max = 50, message = "productIds must not exceed 50 items")
    val productIds: List<Long> = emptyList()
)

data class CompatibilityResponse(
    val compatible: Boolean,
    val issues: List<IssueItem>
) {
    data class IssueItem(val ruleId: Long?, val severity: String, val messageCode: String)
}

// ── Categories ────────────────────────────────────────────────────────────────

data class CategoryItem(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val slug: String
)
