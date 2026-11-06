package com.rest.controller.search.adapter

import com.data.entity.SearchDocument
import com.rest.controller.search.data.SearchResultResponse
import com.rest.util.ApiTypeAdapter

object SearchResultResponseAdapter : ApiTypeAdapter<SearchResultResponse, SearchDocument>() {
    override fun SearchDocument.toApiModel() = SearchResultResponse(
        productId = productId, sku = sku, name = name, brand = brand,
        categoryId = categoryId, status = status, indexedAt = indexedAt
    )
}
