package com.service.price

import com.data.entity.DiscountType
import com.data.entity.Price
import com.data.entity.PriceHistory
import com.data.repository.DiscountRepository
import com.data.repository.PriceHistoryRepository
import com.data.repository.PriceRepository
import com.service.exchangeRate.ExchangeRateService
import com.service.price.dto.BatchCalculatePriceDto
import com.service.price.dto.BatchCalculateResultDto
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
    private val discountRepository: DiscountRepository,
    private val exchangeRateService: ExchangeRateService
) {

    fun findCurrentByProductId(productId: Long): List<Price> =
        priceRepository.findCurrentByProductId(productId, Instant.now())

    fun findById(id: Long): Price =
        priceRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Price $id not found") }

    fun findHistoryByPriceId(id: Long): List<PriceHistory> =
        priceHistoryRepository.findByPriceIdOrderByChangedAtDesc(id)

    fun findBatchByProductIds(productIds: List<Long>): List<Price> =
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
    fun update(id: Long, dto: UpdatePriceDto): Price {
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

    @Transactional(readOnly = true)
    fun calculateBatch(dto: BatchCalculatePriceDto): BatchCalculateResultDto {
        val now = Instant.now()
        val productIds = dto.items.map { it.productId }.distinct()
        val storeIds = dto.items.mapNotNull { it.storeId }.distinct()

        val allPrices = priceRepository.findCurrentByProductIdIn(productIds, now)
        val pricesByProduct = allPrices.groupBy { it.productId }

        val allDiscounts = if (storeIds.isNotEmpty())
            discountRepository.findActiveForStores(storeIds, productIds, now)
        else emptyList()

        // Prefetch all needed exchange rates for this batch (fail fast on missing rate)
        val conversionPairs = dto.items
            .filter { it.targetCurrencyCode != null && it.targetCurrencyCode != it.currencyCode }
            .map { it.currencyCode to it.targetCurrencyCode!! }
            .distinct()
        val rateCache = conversionPairs.associate { (from, to) ->
            (from to to) to exchangeRateService.getRate(from, to)
        }

        val results = mutableListOf<BatchCalculateResultDto.ResultItem>()
        val notFoundItems = mutableListOf<BatchCalculateResultDto.NotFoundItem>()

        for (item in dto.items) {
            val productPrices = pricesByProduct[item.productId]
            val base = productPrices?.firstOrNull { it.storeId == item.storeId && it.currencyCode == item.currencyCode }
                ?: productPrices?.firstOrNull { it.storeId == null && it.currencyCode == item.currencyCode }

            if (base == null) {
                notFoundItems.add(BatchCalculateResultDto.NotFoundItem(item.productId, item.storeId, item.currencyCode))
                continue
            }

            val bestDiscount = if (item.storeId != null) {
                allDiscounts
                    .filter { it.storeId == item.storeId && (it.productId == null || it.productId == item.productId) }
                    .maxByOrNull { it.discountValue }
            } else null

            val discountedAmount = if (bestDiscount != null) {
                when (bestDiscount.discountType) {
                    DiscountType.PERCENTAGE ->
                        base.amount.multiply(BigDecimal.ONE.subtract(bestDiscount.discountValue.divide(BigDecimal(100))))
                            .setScale(4, RoundingMode.HALF_UP)
                    DiscountType.FIXED ->
                        (base.amount - bestDiscount.discountValue).coerceAtLeast(BigDecimal.ZERO)
                            .setScale(4, RoundingMode.HALF_UP)
                }
            } else base.amount

            val targetCurrency = item.targetCurrencyCode ?: item.currencyCode
            val (finalOriginal, finalAmount, finalCurrency) = if (targetCurrency != item.currencyCode) {
                val rate = rateCache.getValue(item.currencyCode to targetCurrency)
                Triple(
                    base.amount.multiply(rate).setScale(4, RoundingMode.HALF_UP),
                    discountedAmount.multiply(rate).setScale(4, RoundingMode.HALF_UP),
                    targetCurrency
                )
            } else Triple(base.amount, discountedAmount, item.currencyCode)

            results.add(
                BatchCalculateResultDto.ResultItem(
                    productId = item.productId,
                    storeId = item.storeId,
                    originalAmount = finalOriginal,
                    finalAmount = finalAmount,
                    currencyCode = finalCurrency,
                    discountApplied = bestDiscount != null
                )
            )
        }

        return BatchCalculateResultDto(results, notFoundItems)
    }
}
