package com.service.storeWebhook.dto

data class CreateStoreWebhookDto(
    val url: String,
    val eventTypes: List<String>,
    val secretRef: String,
    val enabled: Boolean = true
)
