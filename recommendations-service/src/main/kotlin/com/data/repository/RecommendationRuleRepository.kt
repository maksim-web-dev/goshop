package com.data.repository

import com.data.entity.RecommendationRule
import org.springframework.data.jpa.repository.JpaRepository

interface RecommendationRuleRepository : JpaRepository<RecommendationRule, Long> {
    fun findByEnabledTrueOrderByPriorityDesc(): List<RecommendationRule>
}
