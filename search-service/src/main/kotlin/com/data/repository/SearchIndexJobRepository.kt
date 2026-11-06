package com.data.repository

import com.data.entity.IndexJobStatus
import com.data.entity.SearchIndexJob
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SearchIndexJobRepository : JpaRepository<SearchIndexJob, UUID> {
    fun findByStatus(status: IndexJobStatus): List<SearchIndexJob>
}
