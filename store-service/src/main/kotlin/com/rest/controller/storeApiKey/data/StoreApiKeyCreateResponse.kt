package com.rest.controller.storeApiKey.data

import com.data.entity.ApiKeyStatus
import java.time.Instant

data class StoreApiKeyCreateResponse(
    val id: Long?,
    val keyPrefix: String,
    val plainKey: String,
    val status: ApiKeyStatus,
    val createdAt: Instant,
    val expiresAt: Instant?
)
