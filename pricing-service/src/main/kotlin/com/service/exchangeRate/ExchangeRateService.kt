package com.service.exchangeRate

import com.data.entity.ExchangeRate
import com.data.repository.ExchangeRateRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Instant

@Service
class ExchangeRateService(private val exchangeRateRepository: ExchangeRateRepository) {

    fun findAll(): List<ExchangeRate> = exchangeRateRepository.findAll()

    fun getRate(from: String, to: String): BigDecimal {
        if (from == to) return BigDecimal.ONE
        val rate = exchangeRateRepository.findLatest(from, to, Instant.now()).firstOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No exchange rate for $from -> $to")
        return rate.rate
    }
}
