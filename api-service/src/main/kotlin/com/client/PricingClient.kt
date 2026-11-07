package com.client

import com.client.dto.BatchCalculateRequest
import com.client.dto.BatchCalculateResponse
import com.config.props.DownstreamProps
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PricingClient(props: DownstreamProps, builder: RestClient.Builder) {

    private val client = builder.baseUrl(props.pricingServiceUrl).build()

    fun calculateBatch(request: BatchCalculateRequest): BatchCalculateResponse =
        client.post()
            .uri("/api/prices/calculate/batch")
            .body(request)
            .retrieve()
            .body(BatchCalculateResponse::class.java)!!
}
