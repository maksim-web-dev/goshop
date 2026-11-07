package com.rest.controller.search

import com.rest.controller.search.adapter.SearchResultResponseAdapter
import com.rest.controller.search.data.SearchRequest
import com.rest.controller.search.data.SearchResultResponse
import com.service.search.SearchService
import com.service.search.dto.SearchQueryDto
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/search")
@Validated
class SearchController(private val searchService: SearchService) {

    @GetMapping("/products")
    fun search(
        @RequestParam q: String?,
        @RequestParam brand: String?,
        @RequestParam categoryId: Long? = null,
        @RequestParam @Min(0) page: Int = 0,
        @RequestParam @Min(1) @Max(100) size: Int = 20
    ): List<SearchResultResponse> {
        val dto = SearchQueryDto(query = q, brand = brand, categoryId = categoryId, page = page, size = size)
        return with(SearchResultResponseAdapter) { searchService.search(dto).toApiModel() }
    }

    @PostMapping("/products/query")
    fun query(@Valid @RequestBody request: SearchRequest): List<SearchResultResponse> {
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
