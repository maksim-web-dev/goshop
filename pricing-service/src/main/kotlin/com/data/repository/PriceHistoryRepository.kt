package com.data.repository

import com.data.entity.PriceHistory
import org.springframework.data.jpa.repository.JpaRepository

interface PriceHistoryRepository : JpaRepository<PriceHistory, Long> {
    fun findByPriceIdOrderByChangedAtDesc(priceId: Long): List<PriceHistory>
}
