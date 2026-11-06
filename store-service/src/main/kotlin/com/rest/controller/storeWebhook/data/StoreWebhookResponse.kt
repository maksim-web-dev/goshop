package com.rest.controller.storeWebhook.data

data class StoreWebhookResponse(
    val id: Long?,
    val storeId: Long?,
    val url: String,
    val eventTypes: List<String>,
    val secretRef: String,
    val enabled: Boolean
)
