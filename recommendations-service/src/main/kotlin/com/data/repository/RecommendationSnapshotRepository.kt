package com.data.repository

import com.data.entity.RecommendationSnapshot
import com.data.entity.RecommendationStrategy
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecommendationSnapshotRepository : JpaRepository<RecommendationSnapshot, UUID> {
    fun findByProductIdAndStrategy(productId: UUID, strategy: RecommendationStrategy): RecommendationSnapshot?
}
