package com.rest.controller.store

import com.rest.controller.store.adapter.StoreCreateDtoAdapter
import com.rest.controller.store.adapter.StoreResponseAdapter
import com.rest.controller.store.adapter.StoreUpdateDtoAdapter
import com.rest.controller.store.data.StoreCreateRequest
import com.rest.controller.store.data.StoreResponse
import com.rest.controller.store.data.StoreUpdateRequest
import com.service.store.StoreService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stores")
class StoreController(private val storeService: StoreService) {

    @GetMapping
    fun getAll(): List<StoreResponse> =
        with(StoreResponseAdapter) { storeService.findAll().toApiModel() }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): StoreResponse =
        with(StoreResponseAdapter) { storeService.findById(id).toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: StoreCreateRequest): StoreResponse {
        val dto = with(StoreCreateDtoAdapter) { request.toServiceModel() }
        return with(StoreResponseAdapter) { storeService.create(dto).toApiModel() }
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: StoreUpdateRequest): StoreResponse {
        val dto = with(StoreUpdateDtoAdapter) { request.toServiceModel() }
        return with(StoreResponseAdapter) { storeService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deactivate(@PathVariable id: Long) = storeService.deactivate(id)
}
