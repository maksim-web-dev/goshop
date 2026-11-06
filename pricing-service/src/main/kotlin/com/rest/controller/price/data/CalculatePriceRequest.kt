package com.rest.controller.price.data

data class CalculatePriceRequest(
    val productId: Long,
    val storeId: Long? = null,
    val currencyCode: String
)
