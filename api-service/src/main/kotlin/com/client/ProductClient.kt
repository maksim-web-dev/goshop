package com.client

import com.client.dto.CategoryResponse
import com.client.dto.ProductBatchRequest
import com.client.dto.ProductBatchResponse
import com.client.dto.ProductDetailsResponse
import com.config.props.DownstreamProps
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ProductClient(props: DownstreamProps, builder: RestClient.Builder) {

    private val client = builder.baseUrl(props.productServiceUrl).build()

    fun getDetails(productId: Long): ProductDetailsResponse =
        client.get()
            .uri("/api/products/{id}/details", productId)
            .retrieve()
            .body(ProductDetailsResponse::class.java)!!

    fun getBatch(productIds: List<Long>): ProductBatchResponse =
        client.post()
            .uri("/api/products/batch")
            .body(ProductBatchRequest(productIds))
            .retrieve()
            .body(ProductBatchResponse::class.java)!!

    fun getCategories(): List<CategoryResponse> =
        client.get()
            .uri("/api/categories")
            .retrieve()
            .body(object : ParameterizedTypeReference<List<CategoryResponse>>() {})!!
}
