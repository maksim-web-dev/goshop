package com.rest.controller.storeWebhook.data

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class StoreWebhookCreateRequest(
    @field:NotBlank
    val url: String,

    @field:NotEmpty
    val eventTypes: List<String>,

    @field:NotBlank
    val secretRef: String,

    val enabled: Boolean = true
)
