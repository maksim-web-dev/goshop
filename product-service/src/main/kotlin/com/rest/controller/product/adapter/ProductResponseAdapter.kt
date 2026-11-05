package com.rest.controller.product.adapter

import com.data.entity.Product
import com.rest.controller.product.data.ProductResponse
import com.rest.util.ApiTypeAdapter

object ProductResponseAdapter : ApiTypeAdapter<ProductResponse, Product>() {

    override fun Product.toApiModel() = ProductResponse(
        id = id,
        sku = sku,
        name = name,
        brandId = brand.id,
        categoryId = category.id,
        productTypeId = productType.id,
        description = description,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}