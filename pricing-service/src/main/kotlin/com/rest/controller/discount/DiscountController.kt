package com.rest.controller.discount

import com.rest.controller.discount.adapter.DiscountCreateDtoAdapter
import com.rest.controller.discount.adapter.DiscountResponseAdapter
import com.rest.controller.discount.adapter.DiscountUpdateDtoAdapter
import com.rest.controller.discount.data.DiscountCreateRequest
import com.rest.controller.discount.data.DiscountResponse
import com.rest.controller.discount.data.DiscountUpdateRequest
import com.service.discount.DiscountService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/discounts")
class DiscountController(private val discountService: DiscountService) {

    @GetMapping
    fun getAll(): List<DiscountResponse> =
        with(DiscountResponseAdapter) { discountService.findAll().toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: DiscountCreateRequest): DiscountResponse {
        val dto = with(DiscountCreateDtoAdapter) { request.toServiceModel() }
        return with(DiscountResponseAdapter) { discountService.create(dto).toApiModel() }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody request: DiscountUpdateRequest): DiscountResponse {
        val dto = with(DiscountUpdateDtoAdapter) { request.toServiceModel() }
        return with(DiscountResponseAdapter) { discountService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun disable(@PathVariable id: UUID) = discountService.disable(id)
}
