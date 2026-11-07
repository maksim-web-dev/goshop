package com.client

import com.client.dto.SearchResultResponse
import com.config.props.DownstreamProps
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder

@Component
class SearchClient(props: DownstreamProps, builder: RestClient.Builder) {

    private val client = builder.baseUrl(props.searchServiceUrl).build()

    fun search(q: String?, brand: String?, categoryId: Long?, page: Int, size: Int): List<SearchResultResponse> {
        val uri = UriComponentsBuilder.fromPath("/api/search/products")
            .apply { q?.let { queryParam("q", it) } }
            .apply { brand?.let { queryParam("brand", it) } }
            .apply { categoryId?.let { queryParam("categoryId", it) } }
            .queryParam("page", page)
            .queryParam("size", size)
            .build().toUriString()
        return client.get()
            .uri(uri)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<SearchResultResponse>>() {})!!
    }
}
