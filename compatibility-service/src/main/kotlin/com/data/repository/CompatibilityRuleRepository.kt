package com.data.repository

import com.data.entity.CompatibilityRule
import com.data.entity.RuleType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompatibilityRuleRepository : JpaRepository<CompatibilityRule, UUID> {
    fun findByEnabledTrue(): List<CompatibilityRule>
    fun findByRuleType(ruleType: RuleType): List<CompatibilityRule>
    fun existsByCode(code: String): Boolean
}
