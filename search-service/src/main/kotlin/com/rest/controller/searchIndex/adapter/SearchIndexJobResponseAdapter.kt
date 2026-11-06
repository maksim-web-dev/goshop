package com.rest.controller.searchIndex.adapter

import com.data.entity.SearchIndexJob
import com.rest.controller.searchIndex.data.SearchIndexJobResponse
import com.rest.util.ApiTypeAdapter

object SearchIndexJobResponseAdapter : ApiTypeAdapter<SearchIndexJobResponse, SearchIndexJob>() {
    override fun SearchIndexJob.toApiModel() = SearchIndexJobResponse(
        id = id, productId = productId, operation = operation,
        status = status, attempts = attempts, createdAt = createdAt
    )
}
