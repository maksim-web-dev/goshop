package com.data.repository

import com.data.entity.IndexJobStatus
import com.data.entity.SearchIndexJob
import org.springframework.data.jpa.repository.JpaRepository

interface SearchIndexJobRepository : JpaRepository<SearchIndexJob, Long> {
    fun findByStatus(status: IndexJobStatus): List<SearchIndexJob>
}
