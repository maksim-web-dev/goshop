package com.data.repository

import com.data.entity.Discount
import com.data.entity.DiscountStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface DiscountRepository : JpaRepository<Discount, UUID> {
    fun findByStatus(status: DiscountStatus): List<Discount>

    @Query("""
        SELECT d FROM Discount d
        WHERE d.storeId = :storeId
          AND (d.productId IS NULL OR d.productId = :productId)
          AND d.status = 'ACTIVE'
          AND d.startsAt <= :now
          AND (d.endsAt IS NULL OR d.endsAt > :now)
        ORDER BY d.discountValue DESC
    """)
    fun findActiveForProduct(storeId: UUID, productId: UUID, now: Instant): List<Discount>
}
