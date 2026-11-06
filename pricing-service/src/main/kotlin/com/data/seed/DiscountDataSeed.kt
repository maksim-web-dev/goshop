package com.data.seed

import com.config.props.SeedProps
import com.data.entity.Discount
import com.data.entity.DiscountType
import com.data.repository.DiscountRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Component
@Transactional
@Order(3)
class DiscountDataSeed(
    private val discountRepository: DiscountRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (discountRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/pricing_discount.json")
        val dtos: Array<DiscountDto> = jacksonObjectMapper().readValue(json)
        discountRepository.saveAll(dtos.map {
            Discount(
                storeId = UUID.fromString(it.storeId),
                productId = it.productId?.let { id -> UUID.fromString(id) },
                discountType = DiscountType.valueOf(it.discountType),
                discountValue = BigDecimal(it.discountValue),
                startsAt = Instant.parse(it.startsAt),
                endsAt = it.endsAt?.let { d -> Instant.parse(d) }
            )
        })
    }

    private data class DiscountDto(
        @param:JsonProperty("store_id")       val storeId: String,
        @param:JsonProperty("product_id")     val productId: String? = null,
        @param:JsonProperty("discount_type")  val discountType: String,
        @param:JsonProperty("discount_value") val discountValue: String,
        @param:JsonProperty("starts_at")      val startsAt: String,
        @param:JsonProperty("ends_at")        val endsAt: String? = null
    )
}
