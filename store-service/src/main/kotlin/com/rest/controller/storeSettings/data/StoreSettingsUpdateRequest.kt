package com.rest.controller.storeSettings.data

data class StoreSettingsUpdateRequest(
    val requestsPerSecond: Int? = null,
    val burstLimit: Int? = null,
    val allowedLanguages: List<String>? = null
)
