package com.rest.controller.storeWebhook.adapter

import com.data.entity.StoreWebhook
import com.rest.controller.storeWebhook.data.StoreWebhookResponse
import com.rest.util.ApiTypeAdapter

object StoreWebhookResponseAdapter : ApiTypeAdapter<StoreWebhookResponse, StoreWebhook>() {
    override fun StoreWebhook.toApiModel() = StoreWebhookResponse(
        id = id, storeId = store.id, url = url,
        eventTypes = eventTypes.toList(), secretRef = secretRef, enabled = enabled
    )
}
