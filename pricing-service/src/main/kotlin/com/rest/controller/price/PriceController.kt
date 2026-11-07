package com.rest.controller.price

import com.rest.controller.price.adapter.PriceCreateDtoAdapter
import com.rest.controller.price.adapter.PriceHistoryResponseAdapter
import com.rest.controller.price.adapter.PriceResponseAdapter
import com.rest.controller.price.adapter.PriceUpdateDtoAdapter
import com.rest.controller.price.data.*
import com.service.price.PriceService
import com.service.price.dto.BatchCalculatePriceDto
import com.service.price.dto.CalculatePriceDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/prices")
class PriceController(private val priceService: PriceService) {

    @GetMapping("/products/{productId}")
    fun getByProduct(@PathVariable productId: Long): List<PriceResponse> =
        with(PriceResponseAdapter) { priceService.findCurrentByProductId(productId).toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: PriceCreateRequest): PriceResponse {
        val dto = with(PriceCreateDtoAdapter) { request.toServiceModel() }
        return with(PriceResponseAdapter) { priceService.create(dto).toApiModel() }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: PriceUpdateRequest): PriceResponse {
        val dto = with(PriceUpdateDtoAdapter) { request.toServiceModel() }
        return with(PriceResponseAdapter) { priceService.update(id, dto).toApiModel() }
    }

    @GetMapping("/{id}/history")
    fun getHistory(@PathVariable id: Long): List<PriceHistoryResponse> =
        with(PriceHistoryResponseAdapter) { priceService.findHistoryByPriceId(id).toApiModel() }

    @PostMapping("/batch")
    fun getBatch(@Valid @RequestBody request: BatchPriceRequest): List<PriceResponse> =
        with(PriceResponseAdapter) { priceService.findBatchByProductIds(request.productIds).toApiModel() }

    @PostMapping("/calculate")
    fun calculate(@RequestBody request: CalculatePriceRequest): CalculatePriceResponse {
        val result = priceService.calculate(CalculatePriceDto(request.productId, request.storeId, request.currencyCode))
        return CalculatePriceResponse(result.originalAmount, result.finalAmount, result.currencyCode, result.discountApplied)
    }

    @PostMapping("/calculate/batch")
    fun calculateBatch(@Valid @RequestBody request: BatchCalculateRequest): BatchCalculateResponse {
        val dto = BatchCalculatePriceDto(
            items = request.items.map {
                BatchCalculatePriceDto.Item(it.productId, it.storeId, it.currencyCode, it.targetCurrencyCode)
            }
        )
        val result = priceService.calculateBatch(dto)
        return BatchCalculateResponse(
            results = result.results.map {
                BatchCalculateResponse.ResultItem(it.productId, it.storeId, it.originalAmount, it.finalAmount, it.currencyCode, it.discountApplied)
            },
            notFoundItems = result.notFoundItems.map {
                BatchCalculateResponse.NotFoundItem(it.productId, it.storeId, it.currencyCode)
            }
        )
    }
}
