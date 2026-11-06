package com.rest.controller.storeApiKey

import com.rest.controller.storeApiKey.data.StoreApiKeyCreateRequest
import com.rest.controller.storeApiKey.data.StoreApiKeyCreateResponse
import com.service.storeApiKey.StoreApiKeyService
import com.service.storeApiKey.dto.CreateStoreApiKeyDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stores/{storeId}/api-keys")
class StoreApiKeyController(private val storeApiKeyService: StoreApiKeyService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable storeId: Long,
        @RequestBody request: StoreApiKeyCreateRequest
    ): StoreApiKeyCreateResponse {
        val result = storeApiKeyService.create(storeId, CreateStoreApiKeyDto(expiresAt = request.expiresAt))
        return StoreApiKeyCreateResponse(
            id = result.entity.id,
            keyPrefix = result.entity.keyPrefix,
            plainKey = result.plainKey,
            status = result.entity.status,
            createdAt = result.entity.createdAt,
            expiresAt = result.entity.expiresAt
        )
    }

    @PostMapping("/{keyId}/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revoke(@PathVariable storeId: Long, @PathVariable keyId: Long) =
        storeApiKeyService.revoke(storeId, keyId)
}
