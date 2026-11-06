package com.service.price.dto

data class CalculatePriceDto(
    val productId: Long,
    val storeId: Long? = null,
    val currencyCode: String
)
