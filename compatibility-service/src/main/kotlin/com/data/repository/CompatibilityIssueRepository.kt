package com.data.repository

import com.data.entity.CompatibilityIssue
import org.springframework.data.jpa.repository.JpaRepository

interface CompatibilityIssueRepository : JpaRepository<CompatibilityIssue, Long> {
    fun findByCheckId(checkId: Long): List<CompatibilityIssue>
}
