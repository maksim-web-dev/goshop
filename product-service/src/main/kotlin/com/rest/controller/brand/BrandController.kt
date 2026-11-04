package com.rest.controller.brand

import com.data.entity.Brand
import com.rest.controller.brand.adapter.toDto
import com.rest.controller.brand.data.CreateBrandRequest
import com.rest.controller.brand.data.UpdateBrandRequest
import com.service.brand.BrandService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/brands")
class BrandController(private val brandService: BrandService) {

    @GetMapping
    fun getAll(): List<Brand> = brandService.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Brand = brandService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateBrandRequest): Brand =
        brandService.create(request.toDto())

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: UpdateBrandRequest): Brand =
        brandService.update(id, request.toDto())

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = brandService.delete(id)
}