package com.service.storeApiKey.dto

import java.time.Instant

data class CreateStoreApiKeyDto(
    val expiresAt: Instant? = null
)
