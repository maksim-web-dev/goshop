package com.data.repository

import com.data.entity.RecommendationSnapshot
import com.data.entity.RecommendationStrategy
import org.springframework.data.jpa.repository.JpaRepository

interface RecommendationSnapshotRepository : JpaRepository<RecommendationSnapshot, Long> {
    fun findByProductIdAndStrategy(productId: Long, strategy: RecommendationStrategy): RecommendationSnapshot?
}
