package com.client.dto

import java.math.BigDecimal

data class BatchCalculateRequest(val items: List<Item>) {
    data class Item(
        val productId: Long,
        val storeId: Long?,
        val currencyCode: String,
        val targetCurrencyCode: String? = null
    )
}

data class BatchCalculateResponse(
    val results: List<ResultItem>,
    val notFoundItems: List<NotFoundItem>
) {
    data class ResultItem(
        val productId: Long,
        val storeId: Long?,
        val originalAmount: BigDecimal,
        val finalAmount: BigDecimal,
        val currencyCode: String,
        val discountApplied: Boolean
    )
    data class NotFoundItem(val productId: Long, val storeId: Long?, val currencyCode: String)
}
