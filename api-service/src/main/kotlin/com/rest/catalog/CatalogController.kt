package com.rest.catalog

import com.client.CompatibilityClient
import com.client.LocalizationClient
import com.client.PricingClient
import com.client.ProductClient
import com.client.RecommendationsClient
import com.client.SearchClient
import com.client.dto.BatchCalculateRequest
import com.client.dto.BatchResolveRequest
import com.filter.STORE_CONTEXT_ATTR
import com.filter.StoreContext
import com.rest.catalog.data.BatchProductItem
import com.rest.catalog.data.BatchProductsRequest
import com.rest.catalog.data.BatchProductsResponse
import com.rest.catalog.data.CatalogSearchResponse
import com.rest.catalog.data.CategoryItem
import com.rest.catalog.data.CompatibilityRequest
import com.rest.catalog.data.CompatibilityResponse
import com.rest.catalog.data.ProductDetailResponse
import com.rest.catalog.data.RecommendedProductItem
import com.rest.catalog.data.RecommendationsResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@RestController
@RequestMapping("/api/v1/catalog")
@Validated
class CatalogController(
    private val productClient: ProductClient,
    private val localizationClient: LocalizationClient,
    private val pricingClient: PricingClient,
    private val recommendationsClient: RecommendationsClient,
    private val compatibilityClient: CompatibilityClient,
    private val searchClient: SearchClient
) {
    private val executor = Executors.newVirtualThreadPerTaskExecutor()

    // ── A. Product Detail ─────────────────────────────────────────────────────

    @GetMapping("/products/{id}")
    fun getProductDetail(
        @PathVariable id: Long,
        @RequestParam lang: String? = null,
        @RequestParam currency: String? = null,
        @RequestParam(defaultValue = "false") includeRecommendations: Boolean,
        @RequestParam(defaultValue = "true") includeAttributes: Boolean,
        httpRequest: HttpServletRequest
    ): ProductDetailResponse {
        val store = storeContext(httpRequest)
        val effectiveCurrency = currency ?: store.defaultCurrency

        val detailsFuture = CompletableFuture.supplyAsync({ productClient.getDetails(id) }, executor)

        val priceFuture = CompletableFuture.supplyAsync({
            runCatching {
                pricingClient.calculateBatch(
                    BatchCalculateRequest(listOf(BatchCalculateRequest.Item(id, store.storeId, effectiveCurrency)))
                ).results.firstOrNull()
            }.getOrNull()
        }, executor)

        val recommendationsFuture = if (includeRecommendations) {
            CompletableFuture.supplyAsync({
                runCatching {
                    val similar = recommendationsClient.getSimilar(id)
                    val accessories = recommendationsClient.getAccessories(id)
                    similar to accessories
                }.getOrElse { emptyList<Long>() to emptyList() }
            }, executor)
        } else null

        val details = detailsFuture.get()
        val priceResult = priceFuture.get()
        val recsResult = recommendationsFuture?.get()

        val warnings = mutableListOf<String>()
        if (priceResult == null) warnings.add("Price not available for currency $effectiveCurrency")

        val attributes: List<ProductDetailResponse.AttributeInfo> = when {
            includeAttributes -> details.attributes.map { attr ->
                ProductDetailResponse.AttributeInfo(code = attr.code, name = attr.name, value = attr.value)
            }
            else -> emptyList()
        }

        return ProductDetailResponse(
            product = ProductDetailResponse.ProductInfo(
                id = details.id,
                sku = details.sku,
                name = details.name,
                description = details.description,
                status = details.status,
                brand = ProductDetailResponse.BrandInfo(details.brand.id, details.brand.name, details.brand.websiteUrl),
                category = ProductDetailResponse.CategoryInfo(details.category.id, details.category.name, details.category.slug),
                productType = ProductDetailResponse.ProductTypeInfo(details.productType.id, details.productType.name),
                attributes = attributes,
                createdAt = details.createdAt,
                updatedAt = details.updatedAt
            ),
            price = priceResult?.let {
                ProductDetailResponse.PriceInfo(it.finalAmount, it.originalAmount, it.currencyCode, it.discountApplied)
            },
            recommendations = recsResult?.let { (similar, accessories) ->
                ProductDetailResponse.RecommendationsInfo(similar, accessories)
            },
            warnings = warnings
        )
    }

    // ── B. Batch Products ─────────────────────────────────────────────────────

    @PostMapping("/products/batch")
    fun getBatchProducts(
        @Valid @RequestBody request: BatchProductsRequest,
        httpRequest: HttpServletRequest
    ): BatchProductsResponse {
        val store = storeContext(httpRequest)
        val effectiveLang = request.lang ?: store.defaultLanguage

        val batchFuture = CompletableFuture.supplyAsync(
            { productClient.getBatch(request.productIds) }, executor
        )

        val batch = batchFuture.get()
        val foundProducts = batch.products

        val translationsFuture = if (foundProducts.isNotEmpty()) {
            CompletableFuture.supplyAsync({
                runCatching {
                    localizationClient.batchResolve(
                        BatchResolveRequest(
                            entityType = "PRODUCT",
                            entityIds = foundProducts.map { it.id },
                            fieldNames = listOf("name"),
                            languageCode = effectiveLang
                        )
                    )
                }.getOrNull()
            }, executor)
        } else null

        val pricesFuture = if (request.includePrices && foundProducts.isNotEmpty()) {
            CompletableFuture.supplyAsync({
                runCatching {
                    pricingClient.calculateBatch(
                        BatchCalculateRequest(
                            foundProducts.map {
                                BatchCalculateRequest.Item(it.id, store.storeId, store.defaultCurrency)
                            }
                        )
                    )
                }.getOrNull()
            }, executor)
        } else null

        val translations = translationsFuture?.get()
        val prices = pricesFuture?.get()

        val translationMap = translations?.translations
            ?.filter { it.fieldName == "name" }
            ?.associate { it.entityId to it.value }
            ?: emptyMap()

        val priceMap = prices?.results?.associate { it.productId to it } ?: emptyMap()

        val items = foundProducts.map { p ->
            val priceResult = priceMap[p.id]
            BatchProductItem(
                id = p.id,
                sku = p.sku,
                name = p.name,
                translatedName = translationMap[p.id],
                description = p.description,
                status = p.status,
                brandId = p.brandId,
                categoryId = p.categoryId,
                price = priceResult?.let {
                    ProductDetailResponse.PriceInfo(it.finalAmount, it.originalAmount, it.currencyCode, it.discountApplied)
                }
            )
        }
        return BatchProductsResponse(products = items, notFoundIds = batch.notFoundIds)
    }

    // ── C. Catalog Search ─────────────────────────────────────────────────────

    @GetMapping("/search")
    fun search(
        @RequestParam q: String? = null,
        @RequestParam categoryId: Long? = null,
        @RequestParam brand: String? = null,
        @RequestParam lang: String? = null,
        @RequestParam @Min(0) page: Int = 0,
        @RequestParam @Min(1) @Max(100) size: Int = 20,
        httpRequest: HttpServletRequest
    ): CatalogSearchResponse {
        val store = storeContext(httpRequest)
        val effectiveLang = lang ?: store.defaultLanguage

        val searchResults = searchClient.search(q, brand, categoryId, page, size)
        val productIds = searchResults.map { it.productId }

        val translationsMap = if (productIds.isNotEmpty()) {
            runCatching {
                localizationClient.batchResolve(
                    BatchResolveRequest(
                        entityType = "PRODUCT",
                        entityIds = productIds,
                        fieldNames = listOf("name"),
                        languageCode = effectiveLang
                    )
                ).translations.filter { it.fieldName == "name" }.associate { it.entityId to it.value }
            }.getOrElse { emptyMap() }
        } else emptyMap()

        val items = searchResults.map { r ->
            CatalogSearchResponse.SearchItem(
                productId = r.productId,
                sku = r.sku,
                name = r.name,
                translatedName = translationsMap[r.productId],
                brand = r.brand,
                categoryId = r.categoryId,
                status = r.status
            )
        }
        return CatalogSearchResponse(results = items, page = page, size = size)
    }

    // ── D. Recommendations ────────────────────────────────────────────────────

    @GetMapping("/products/{id}/recommendations")
    fun getRecommendations(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "SIMILAR") type: String,
        @RequestParam @Min(1) @Max(50) limit: Int = 10,
        @RequestParam lang: String? = null,
        httpRequest: HttpServletRequest
    ): RecommendationsResponse {
        val store = storeContext(httpRequest)
        val effectiveLang = lang ?: store.defaultLanguage

        val recommendedIds = when (type.uppercase()) {
            "SIMILAR" -> recommendationsClient.getSimilar(id).take(limit)
            "ALTERNATIVES" -> recommendationsClient.getAlternatives(id).take(limit)
            "ACCESSORIES" -> recommendationsClient.getAccessories(id).take(limit)
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown recommendation type: $type. Valid: SIMILAR, ALTERNATIVES, ACCESSORIES")
        }

        if (recommendedIds.isEmpty()) return RecommendationsResponse(emptyList())

        val batchFuture = CompletableFuture.supplyAsync(
            { productClient.getBatch(recommendedIds) }, executor
        )
        val pricesFuture = CompletableFuture.supplyAsync({
            runCatching {
                pricingClient.calculateBatch(
                    BatchCalculateRequest(
                        recommendedIds.map { BatchCalculateRequest.Item(it, store.storeId, store.defaultCurrency) }
                    )
                )
            }.getOrNull()
        }, executor)
        val translationsFuture = CompletableFuture.supplyAsync({
            runCatching {
                localizationClient.batchResolve(
                    BatchResolveRequest("PRODUCT", recommendedIds, listOf("name"), effectiveLang)
                )
            }.getOrNull()
        }, executor)

        val batch = batchFuture.get()
        val prices = pricesFuture.get()
        val translations = translationsFuture.get()

        val priceMap = prices?.results?.associate { it.productId to it } ?: emptyMap()
        val nameMap = translations?.translations
            ?.filter { it.fieldName == "name" }
            ?.associate { it.entityId to it.value }
            ?: emptyMap()

        val items = batch.products.map { p ->
            val priceResult = priceMap[p.id]
            RecommendedProductItem(
                id = p.id,
                sku = p.sku,
                name = p.name,
                translatedName = nameMap[p.id],
                brandId = p.brandId,
                categoryId = p.categoryId,
                status = p.status,
                price = priceResult?.let {
                    ProductDetailResponse.PriceInfo(it.finalAmount, it.originalAmount, it.currencyCode, it.discountApplied)
                }
            )
        }
        return RecommendationsResponse(items)
    }

    // ── E. Compatibility ──────────────────────────────────────────────────────

    @PostMapping("/compatibility/check")
    fun checkCompatibility(
        @Valid @RequestBody request: CompatibilityRequest,
        httpRequest: HttpServletRequest
    ): CompatibilityResponse {
        storeContext(httpRequest)
        val result = compatibilityClient.check(request.productIds)
        return CompatibilityResponse(
            compatible = result.compatible,
            issues = result.issues.map {
                CompatibilityResponse.IssueItem(it.ruleId, it.severity, it.messageCode)
            }
        )
    }

    // ── F. Categories ─────────────────────────────────────────────────────────

    @GetMapping("/categories")
    fun getCategories(httpRequest: HttpServletRequest): List<CategoryItem> {
        storeContext(httpRequest)
        return productClient.getCategories().map {
            CategoryItem(id = it.id, parentId = it.parentId, name = it.name, slug = it.slug)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun storeContext(request: HttpServletRequest): StoreContext =
        request.getAttribute(STORE_CONTEXT_ATTR) as? StoreContext
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Store context missing")
}
