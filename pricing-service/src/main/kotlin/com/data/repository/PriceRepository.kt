package com.data.repository

import com.data.entity.Price
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface PriceRepository : JpaRepository<Price, Long> {
    @Query("""
        SELECT p FROM Price p
        WHERE p.productId = :productId
          AND (p.validTo IS NULL OR p.validTo > :now)
        ORDER BY p.storeId NULLS LAST, p.validFrom DESC
    """)
    fun findCurrentByProductId(productId: Long, now: Instant): List<Price>

    fun findByProductIdIn(productIds: List<Long>): List<Price>

    @Query("""
        SELECT p FROM Price p
        WHERE p.productId IN :productIds
          AND (p.validTo IS NULL OR p.validTo > :now)
        ORDER BY p.storeId NULLS LAST, p.validFrom DESC
    """)
    fun findCurrentByProductIdIn(productIds: List<Long>, now: Instant): List<Price>
}
