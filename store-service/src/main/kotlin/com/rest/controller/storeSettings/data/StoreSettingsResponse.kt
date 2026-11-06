package com.rest.controller.storeSettings.data

data class StoreSettingsResponse(
    val id: Long?,
    val storeId: Long?,
    val requestsPerSecond: Int,
    val burstLimit: Int,
    val allowedLanguages: List<String>
)
