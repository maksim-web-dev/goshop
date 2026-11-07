package com.client

import com.client.dto.ApiKeyValidateRequest
import com.client.dto.ApiKeyValidateResponse
import com.config.props.DownstreamProps
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class StoreClient(props: DownstreamProps, builder: RestClient.Builder) {

    private val client = builder.baseUrl(props.storeServiceUrl).build()

    fun validateApiKey(apiKey: String): ApiKeyValidateResponse =
        client.post()
            .uri("/api/internal/api-keys/validate")
            .body(ApiKeyValidateRequest(apiKey))
            .retrieve()
            .body(ApiKeyValidateResponse::class.java)!!
}
