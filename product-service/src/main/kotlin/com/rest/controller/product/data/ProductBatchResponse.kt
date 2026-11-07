package com.rest.controller.product.data

data class ProductBatchResponse(
    val products: List<ProductResponse>,
    val notFoundIds: List<Long>
)
