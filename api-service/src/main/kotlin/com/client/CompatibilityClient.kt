package com.client

import com.client.dto.CompatibilityCheckRequest
import com.client.dto.CompatibilityCheckResponse
import com.config.props.DownstreamProps
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class CompatibilityClient(props: DownstreamProps, builder: RestClient.Builder) {

    private val client = builder.baseUrl(props.compatibilityServiceUrl).build()

    fun check(productIds: List<Long>): CompatibilityCheckResponse =
        client.post()
            .uri("/api/compatibility/check")
            .body(CompatibilityCheckRequest(productIds))
            .retrieve()
            .body(CompatibilityCheckResponse::class.java)!!
}
