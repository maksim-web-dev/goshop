package com.rest.controller.discount

import com.rest.controller.discount.adapter.DiscountCreateDtoAdapter
import com.rest.controller.discount.adapter.DiscountResponseAdapter
import com.rest.controller.discount.adapter.DiscountUpdateDtoAdapter
import com.rest.controller.discount.data.DiscountCreateRequest
import com.rest.controller.discount.data.DiscountResponse
import com.rest.controller.discount.data.DiscountUpdateRequest
import com.service.discount.DiscountService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@RestController
@RequestMapping("/discounts")
@Validated
class DiscountController(private val discountService: DiscountService) {

    @GetMapping
    fun getAll(
        @RequestParam @Min(0) page: Int = 0,
        @RequestParam @Min(1) @Max(100) size: Int = 20
    ): Page<DiscountResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startsAt"))
        return discountService.findAll(pageable)
            .map { with(DiscountResponseAdapter) { it.toApiModel() } }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: DiscountCreateRequest): DiscountResponse {
        val dto = with(DiscountCreateDtoAdapter) { request.toServiceModel() }
        return with(DiscountResponseAdapter) { discountService.create(dto).toApiModel() }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: DiscountUpdateRequest): DiscountResponse {
        val dto = with(DiscountUpdateDtoAdapter) { request.toServiceModel() }
        return with(DiscountResponseAdapter) { discountService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun disable(@PathVariable id: Long) = discountService.disable(id)
}
