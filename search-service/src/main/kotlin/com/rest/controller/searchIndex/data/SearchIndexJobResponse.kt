package com.rest.controller.searchIndex.data

import com.data.entity.IndexJobStatus
import com.data.entity.IndexOperation
import java.time.Instant
import java.util.UUID

data class SearchIndexJobResponse(
    val id: UUID,
    val productId: UUID,
    val operation: IndexOperation,
    val status: IndexJobStatus,
    val attempts: Int,
    val createdAt: Instant
)
