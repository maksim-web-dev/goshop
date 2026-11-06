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
import java.util.UUID

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
                productId = UUID.fromString(it.productId),
                storeId = it.storeId?.let { id -> UUID.fromString(id) },
                amount = BigDecimal(it.amount),
                currencyCode = it.currencyCode
            )
        })
    }

    private data class PriceDto(
        @param:JsonProperty("product_id")    val productId: String,
        @param:JsonProperty("store_id")      val storeId: String? = null,
        @param:JsonProperty("amount")        val amount: String,
        @param:JsonProperty("currency_code") val currencyCode: String
    )
}
