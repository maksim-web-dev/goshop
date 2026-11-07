package com.service.price.dto

data class BatchCalculatePriceDto(
    val items: List<Item>
) {
    data class Item(
        val productId: Long,
        val storeId: Long?,
        val currencyCode: String,
        val targetCurrencyCode: String?
    )
}
