package com.service.price

import com.data.entity.Price
import com.data.entity.PriceHistory
import com.data.repository.DiscountRepository
import com.data.repository.PriceHistoryRepository
import com.data.repository.PriceRepository
import com.service.price.dto.CalculatePriceDto
import com.service.price.dto.CreatePriceDto
import com.service.price.dto.UpdatePriceDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.UUID

data class CalculatedPrice(
    val originalAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val currencyCode: String,
    val discountApplied: Boolean
)

@Service
class PriceService(
    private val priceRepository: PriceRepository,
    private val priceHistoryRepository: PriceHistoryRepository,
    private val discountRepository: DiscountRepository
) {

    fun findCurrentByProductId(productId: UUID): List<Price> =
        priceRepository.findCurrentByProductId(productId, Instant.now())

    fun findById(id: UUID): Price =
        priceRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Price $id not found") }

    fun findHistoryByPriceId(id: UUID): List<PriceHistory> =
        priceHistoryRepository.findByPriceIdOrderByChangedAtDesc(id)

    fun findBatchByProductIds(productIds: List<UUID>): List<Price> =
        priceRepository.findCurrentByProductIdIn(productIds, Instant.now())

    @Transactional
    fun create(dto: CreatePriceDto): Price =
        priceRepository.save(
            Price(
                productId = dto.productId,
                storeId = dto.storeId,
                amount = dto.amount,
                currencyCode = dto.currencyCode,
                validFrom = dto.validFrom,
                validTo = dto.validTo
            )
        )

    @Transactional
    fun update(id: UUID, dto: UpdatePriceDto): Price {
        val existing = findById(id)
        val newAmount = dto.amount ?: existing.amount
        if (dto.amount != null && dto.amount != existing.amount) {
            priceHistoryRepository.save(
                PriceHistory(price = existing, oldAmount = existing.amount, newAmount = newAmount)
            )
        }
        return priceRepository.save(
            Price(
                id = existing.id,
                productId = existing.productId,
                storeId = existing.storeId,
                amount = newAmount,
                currencyCode = dto.currencyCode ?: existing.currencyCode,
                validFrom = dto.validFrom ?: existing.validFrom,
                validTo = dto.validTo ?: existing.validTo,
                updatedAt = Instant.now()
            )
        )
    }

    fun calculate(dto: CalculatePriceDto): CalculatedPrice {
        val prices = priceRepository.findCurrentByProductId(dto.productId, Instant.now())
        val base = prices.firstOrNull { it.storeId == dto.storeId && it.currencyCode == dto.currencyCode }
            ?: prices.firstOrNull { it.storeId == null && it.currencyCode == dto.currencyCode }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No price found for product ${dto.productId}")

        val discounts = dto.storeId?.let {
            discountRepository.findActiveForProduct(it, dto.productId, Instant.now())
        } ?: emptyList()

        val best = discounts.firstOrNull()
        val finalAmount = if (best != null) {
            when (best.discountType) {
                com.data.entity.DiscountType.PERCENTAGE ->
                    base.amount.multiply(BigDecimal.ONE.subtract(best.discountValue.divide(BigDecimal(100))))
                        .setScale(4, RoundingMode.HALF_UP)
                com.data.entity.DiscountType.FIXED ->
                    (base.amount - best.discountValue).coerceAtLeast(BigDecimal.ZERO)
                        .setScale(4, RoundingMode.HALF_UP)
            }
        } else base.amount

        return CalculatedPrice(
            originalAmount = base.amount,
            finalAmount = finalAmount,
            currencyCode = base.currencyCode,
            discountApplied = best != null
        )
    }
}
