package com.rest.controller.searchIndex.data

import com.data.entity.IndexJobStatus
import com.data.entity.IndexOperation
import java.time.Instant

data class SearchIndexJobResponse(
    val id: Long?,
    val productId: Long,
    val operation: IndexOperation,
    val status: IndexJobStatus,
    val attempts: Int,
    val createdAt: Instant
)
