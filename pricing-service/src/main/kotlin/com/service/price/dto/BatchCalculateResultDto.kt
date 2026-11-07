package com.service.price.dto

import java.math.BigDecimal

data class BatchCalculateResultDto(
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

    data class NotFoundItem(
        val productId: Long,
        val storeId: Long?,
        val currencyCode: String
    )
}
