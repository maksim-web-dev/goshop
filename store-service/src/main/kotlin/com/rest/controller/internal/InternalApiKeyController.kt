package com.rest.controller.internal

import com.rest.controller.internal.data.ApiKeyValidateRequest
import com.rest.controller.internal.data.ApiKeyValidateResponse
import com.service.storeApiKey.StoreApiKeyService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/api-keys")
class InternalApiKeyController(private val storeApiKeyService: StoreApiKeyService) {

    @PostMapping("/validate")
    fun validate(@Valid @RequestBody request: ApiKeyValidateRequest): ApiKeyValidateResponse =
        storeApiKeyService.validate(request.apiKey)
}
