package com.rest.controller.search

import com.rest.controller.search.adapter.SearchResultResponseAdapter
import com.rest.controller.search.data.SearchRequest
import com.rest.controller.search.data.SearchResultResponse
import com.service.search.SearchService
import com.service.search.dto.SearchQueryDto
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/search")
class SearchController(private val searchService: SearchService) {

    @GetMapping("/products")
    fun search(
        @RequestParam q: String?,
        @RequestParam brand: String?,
        @RequestParam categoryId: UUID? = null,
        @RequestParam page: Int = 0,
        @RequestParam size: Int = 20
    ): List<SearchResultResponse> {
        val dto = SearchQueryDto(query = q, brand = brand, categoryId = categoryId, page = page, size = size)
        return with(SearchResultResponseAdapter) { searchService.search(dto).toApiModel() }
    }

    @PostMapping("/products/query")
    fun query(@RequestBody request: SearchRequest): List<SearchResultResponse> {
        val dto = SearchQueryDto(
            query = request.query, brand = request.brand, categoryId = request.categoryId,
            minPrice = request.minPrice, maxPrice = request.maxPrice,
            attributes = request.attributes, page = request.page, size = request.size
        )
        return with(SearchResultResponseAdapter) { searchService.search(dto).toApiModel() }
    }

    @GetMapping("/suggestions")
    fun suggestions(@RequestParam prefix: String): List<String> =
        searchService.suggestions(prefix)
}
