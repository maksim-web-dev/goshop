package com.service.search.dto

import java.math.BigDecimal

data class SearchQueryDto(
    val query: String? = null,
    val brand: String? = null,
    val categoryId: Long? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val attributes: Map<String, String> = emptyMap(),
    val page: Int = 0,
    val size: Int = 20
)
