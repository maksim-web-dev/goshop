package com.service.storeSettings.dto

data class UpdateStoreSettingsDto(
    val requestsPerSecond: Int? = null,
    val burstLimit: Int? = null,
    val allowedLanguages: List<String>? = null
)
