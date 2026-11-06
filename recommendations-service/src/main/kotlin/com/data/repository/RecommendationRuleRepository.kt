package com.data.repository

import com.data.entity.RecommendationRule
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecommendationRuleRepository : JpaRepository<RecommendationRule, UUID> {
    fun findByEnabledTrueOrderByPriorityDesc(): List<RecommendationRule>
}
