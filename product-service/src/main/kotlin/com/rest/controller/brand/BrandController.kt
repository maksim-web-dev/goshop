package com.rest.controller.brand

import com.rest.controller.brand.adapter.BrandCreateDtoAdapter
import com.rest.controller.brand.adapter.BrandResponseAdapter
import com.rest.controller.brand.adapter.BrandUpdateDtoAdapter
import com.rest.controller.brand.data.BrandResponse
import com.rest.controller.brand.data.BrandCreateRequest
import com.rest.controller.brand.data.BrandUpdateRequest
import com.service.brand.BrandService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/brands")
class BrandController(private val brandService: BrandService) {

    @GetMapping
    fun getAll(): List<BrandResponse> =
        with(BrandResponseAdapter) { brandService.findAll().toApiModel() }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): BrandResponse =
        with(BrandResponseAdapter) { brandService.findById(id).toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody payload: BrandCreateRequest): BrandResponse {
        val dto = with(BrandCreateDtoAdapter) { payload.toServiceModel() }
        return with(BrandResponseAdapter) { brandService.create(dto).toApiModel() }
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: BrandUpdateRequest): BrandResponse {
        val dto = with(BrandUpdateDtoAdapter) { request.toServiceModel() }
        return with(BrandResponseAdapter) { brandService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = brandService.delete(id)
}