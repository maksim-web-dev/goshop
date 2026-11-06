package com.data.repository

import com.data.entity.Price
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface PriceRepository : JpaRepository<Price, UUID> {
    @Query("""
        SELECT p FROM Price p
        WHERE p.productId = :productId
          AND (p.validTo IS NULL OR p.validTo > :now)
        ORDER BY p.storeId NULLS LAST, p.validFrom DESC
    """)
    fun findCurrentByProductId(productId: UUID, now: Instant): List<Price>

    fun findByProductIdIn(productIds: List<UUID>): List<Price>
}
