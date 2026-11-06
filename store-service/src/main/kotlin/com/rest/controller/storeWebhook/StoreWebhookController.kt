package com.rest.controller.storeWebhook

import com.rest.controller.storeWebhook.adapter.StoreWebhookResponseAdapter
import com.rest.controller.storeWebhook.data.StoreWebhookCreateRequest
import com.rest.controller.storeWebhook.data.StoreWebhookResponse
import com.service.storeWebhook.StoreWebhookService
import com.service.storeWebhook.dto.CreateStoreWebhookDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/stores/{storeId}/webhooks")
class StoreWebhookController(private val storeWebhookService: StoreWebhookService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable storeId: UUID,
        @RequestBody request: StoreWebhookCreateRequest
    ): StoreWebhookResponse {
        val dto = CreateStoreWebhookDto(
            url = request.url, eventTypes = request.eventTypes,
            secretRef = request.secretRef, enabled = request.enabled
        )
        return with(StoreWebhookResponseAdapter) { storeWebhookService.create(storeId, dto).toApiModel() }
    }
}
