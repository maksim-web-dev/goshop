package com.data.seed

import com.config.props.SeedProps
import com.data.entity.Price
import com.data.repository.PriceRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@Component
@Transactional
@Order(2)
class PriceDataSeed(
    private val priceRepository: PriceRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (priceRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/pricing_price.json")
        val dtos: Array<PriceDto> = jacksonObjectMapper().readValue(json)
        priceRepository.saveAll(dtos.map {
            Price(
                productId = it.productId,
                storeId = it.storeId,
                amount = BigDecimal(it.amount),
                currencyCode = it.currencyCode,
                validFrom = Instant.parse(it.validFrom),
                validTo = it.validTo?.let { v -> Instant.parse(v) }
            )
        })
    }

    private data class PriceDto(
        @param:JsonProperty("product_id")    val productId: Long,
        @param:JsonProperty("store_id")      val storeId: Long? = null,
        @param:JsonProperty("amount")        val amount: String,
        @param:JsonProperty("currency_code") val currencyCode: String,
        @param:JsonProperty("valid_from")    val validFrom: String,
        @param:JsonProperty("valid_to")      val validTo: String? = null
    )
}
