package com.client

import com.client.dto.BatchResolveRequest
import com.client.dto.BatchResolveResponse
import com.config.props.DownstreamProps
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class LocalizationClient(props: DownstreamProps, builder: RestClient.Builder) {

    private val client = builder.baseUrl(props.localizationServiceUrl).build()

    fun batchResolve(request: BatchResolveRequest): BatchResolveResponse =
        client.post()
            .uri("/api/translations/batch-resolve")
            .body(request)
            .retrieve()
            .body(BatchResolveResponse::class.java)!!
}
