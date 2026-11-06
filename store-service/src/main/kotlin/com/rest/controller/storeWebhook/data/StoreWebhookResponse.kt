package com.rest.controller.storeWebhook.data

import java.util.UUID

data class StoreWebhookResponse(
    val id: UUID,
    val storeId: UUID,
    val url: String,
    val eventTypes: List<String>,
    val secretRef: String,
    val enabled: Boolean
)
