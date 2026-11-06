package com.data.seed

import com.config.props.SeedProps
import com.data.entity.ExchangeRate
import com.data.repository.ExchangeRateRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
@Transactional
@Order(1)
class ExchangeRateDataSeed(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (exchangeRateRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/pricing_exchange_rate.json")
        val dtos: Array<ExchangeRateDto> = jacksonObjectMapper().readValue(json)
        exchangeRateRepository.saveAll(dtos.map {
            ExchangeRate(
                fromCurrency = it.fromCurrency,
                toCurrency = it.toCurrency,
                rate = BigDecimal(it.rate)
            )
        })
    }

    private data class ExchangeRateDto(
        @param:JsonProperty("from_currency") val fromCurrency: String,
        @param:JsonProperty("to_currency")   val toCurrency: String,
        @param:JsonProperty("rate")          val rate: String
    )
}
