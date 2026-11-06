package com.data.repository

import com.data.entity.TranslationJob
import com.data.entity.TranslationJobStatus
import org.springframework.data.jpa.repository.JpaRepository

interface TranslationJobRepository : JpaRepository<TranslationJob, Long> {
    fun findByStatus(status: TranslationJobStatus): List<TranslationJob>
}
