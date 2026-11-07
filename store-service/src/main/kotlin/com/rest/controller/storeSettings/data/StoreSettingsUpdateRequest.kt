package com.rest.controller.storeSettings.data

import jakarta.validation.constraints.Positive

data class StoreSettingsUpdateRequest(
    @field:Positive
    val requestsPerSecond: Int? = null,

    @field:Positive
    val burstLimit: Int? = null,

    val allowedLanguages: List<String>? = null
)
