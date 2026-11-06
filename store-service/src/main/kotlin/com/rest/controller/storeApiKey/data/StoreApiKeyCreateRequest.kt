package com.rest.controller.storeApiKey.data

import java.time.Instant

data class StoreApiKeyCreateRequest(
    val expiresAt: Instant? = null
)
