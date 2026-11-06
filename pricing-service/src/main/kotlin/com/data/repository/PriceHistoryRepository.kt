package com.data.repository

import com.data.entity.PriceHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PriceHistoryRepository : JpaRepository<PriceHistory, UUID> {
    fun findByPriceIdOrderByChangedAtDesc(priceId: UUID): List<PriceHistory>
}
