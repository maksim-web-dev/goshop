package com.rest.controller.storeWebhook.data

data class StoreWebhookCreateRequest(
    val url: String,
    val eventTypes: List<String>,
    val secretRef: String,
    val enabled: Boolean = true
)
