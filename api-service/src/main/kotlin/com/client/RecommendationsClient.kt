package com.client

import com.config.props.DownstreamProps
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class RecommendationsClient(props: DownstreamProps, builder: RestClient.Builder) {

    private val client = builder.baseUrl(props.recommendationsServiceUrl).build()

    fun getSimilar(productId: Long): List<Long> =
        client.get()
            .uri("/api/recommendations/products/{id}/similar", productId)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<Long>>() {})!!

    fun getAlternatives(productId: Long): List<Long> =
        client.get()
            .uri("/api/recommendations/products/{id}/alternatives", productId)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<Long>>() {})!!

    fun getAccessories(productId: Long): List<Long> =
        client.get()
            .uri("/api/recommendations/products/{id}/accessories", productId)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<Long>>() {})!!

    fun getAll(productId: Long): List<Long> =
        client.get()
            .uri("/api/recommendations/products/{id}", productId)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<Long>>() {})!!
}
