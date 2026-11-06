package com.service.searchIndex

import com.data.entity.IndexJobStatus
import com.data.entity.IndexOperation
import com.data.entity.SearchIndexJob
import com.data.repository.SearchIndexJobRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class SearchIndexJobService(private val jobRepository: SearchIndexJobRepository) {

    fun findById(id: UUID): SearchIndexJob =
        jobRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Job $id not found") }

    @Transactional
    fun enqueue(productId: UUID, operation: IndexOperation): SearchIndexJob =
        jobRepository.save(SearchIndexJob(productId = productId, operation = operation))

    @Transactional
    fun reindexAll(productIds: List<UUID>): List<SearchIndexJob> =
        productIds.map { enqueue(it, IndexOperation.INDEX) }
}
