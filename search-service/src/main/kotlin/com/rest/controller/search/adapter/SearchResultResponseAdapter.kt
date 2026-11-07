package com.rest.controller.search.adapter

import com.data.entity.SearchDocument
import com.rest.controller.search.data.SearchResultResponse
import com.rest.util.ApiTypeAdapter

object SearchResultResponseAdapter : ApiTypeAdapter<SearchResultResponse, SearchDocument>() {
    override fun SearchDocument.toApiModel() = SearchResultResponse(
        productId = productId,
        sku = sku,
        name = name,
        localizedNames = localizedNames,
        brand = brand,
        categoryId = categoryId,
        attributes = attributes,
        status = status,
        indexedAt = indexedAt
    )
}
