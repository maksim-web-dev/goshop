package com.rest.controller.store.data

import com.data.entity.StoreStatus
import java.time.Instant

data class StoreResponse(
    val id: Long?,
    val name: String,
    val domain: String,
    val countryCode: String,
    val defaultLanguage: String,
    val defaultCurrency: String,
    val status: StoreStatus,
    val createdAt: Instant
)
