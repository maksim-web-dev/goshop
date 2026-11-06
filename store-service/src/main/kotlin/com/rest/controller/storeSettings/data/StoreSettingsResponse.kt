package com.rest.controller.storeSettings.data

import java.util.UUID

data class StoreSettingsResponse(
    val id: UUID,
    val storeId: UUID,
    val requestsPerSecond: Int,
    val burstLimit: Int,
    val allowedLanguages: List<String>
)
