package com.rest.controller.searchIndex

import com.data.entity.IndexOperation
import com.rest.controller.searchIndex.adapter.SearchIndexJobResponseAdapter
import com.rest.controller.searchIndex.data.ReindexRequest
import com.rest.controller.searchIndex.data.SearchIndexJobResponse
import com.service.searchIndex.SearchIndexJobService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/internal/search")
class SearchIndexController(private val searchIndexJobService: SearchIndexJobService) {

    @PostMapping("/reindex")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun reindex(@Valid @RequestBody request: ReindexRequest): List<SearchIndexJobResponse> =
        with(SearchIndexJobResponseAdapter) { searchIndexJobService.reindexAll(request.productIds).toApiModel() }

    @GetMapping("/index-jobs/{id}")
    fun getJob(@PathVariable id: Long): SearchIndexJobResponse =
        with(SearchIndexJobResponseAdapter) { searchIndexJobService.findById(id).toApiModel() }
}
