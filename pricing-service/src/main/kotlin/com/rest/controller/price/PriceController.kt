package com.rest.controller.price

import com.rest.controller.price.adapter.PriceCreateDtoAdapter
import com.rest.controller.price.adapter.PriceHistoryResponseAdapter
import com.rest.controller.price.adapter.PriceResponseAdapter
import com.rest.controller.price.adapter.PriceUpdateDtoAdapter
import com.rest.controller.price.data.*
import com.service.price.PriceService
import com.service.price.dto.CalculatePriceDto
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
    fun getBatch(@RequestBody request: BatchPriceRequest): List<PriceResponse> =
        with(PriceResponseAdapter) { priceService.findBatchByProductIds(request.productIds).toApiModel() }

    @PostMapping("/calculate")
    fun calculate(@RequestBody request: CalculatePriceRequest): CalculatePriceResponse {
        val result = priceService.calculate(CalculatePriceDto(request.productId, request.storeId, request.currencyCode))
        return CalculatePriceResponse(result.originalAmount, result.finalAmount, result.currencyCode, result.discountApplied)
    }
}
