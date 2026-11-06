package com.data.repository

import com.data.entity.CompatibilityIssue
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompatibilityIssueRepository : JpaRepository<CompatibilityIssue, UUID> {
    fun findByCheckId(checkId: UUID): List<CompatibilityIssue>
}
