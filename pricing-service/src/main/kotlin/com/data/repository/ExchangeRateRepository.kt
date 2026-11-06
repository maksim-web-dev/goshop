package com.data.repository

import com.data.entity.ExchangeRate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface ExchangeRateRepository : JpaRepository<ExchangeRate, Long> {
    @Query("""
        SELECT e FROM ExchangeRate e
        WHERE e.fromCurrency = :from AND e.toCurrency = :to
          AND e.effectiveAt <= :now
        ORDER BY e.effectiveAt DESC
    """)
    fun findLatest(from: String, to: String, now: Instant): List<ExchangeRate>
}
