package com.rest.controller.storeApiKey.data

import com.data.entity.ApiKeyStatus
import java.time.Instant
import java.util.UUID

data class StoreApiKeyCreateResponse(
    val id: UUID,
    val keyPrefix: String,
    val plainKey: String,
    val status: ApiKeyStatus,
    val createdAt: Instant,
    val expiresAt: Instant?
)
