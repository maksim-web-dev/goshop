package com.data.repository

import com.data.entity.TranslationJob
import com.data.entity.TranslationJobStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TranslationJobRepository : JpaRepository<TranslationJob, UUID> {
    fun findByStatus(status: TranslationJobStatus): List<TranslationJob>
}
