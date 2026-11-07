package com.rest.controller.storeSettings

import com.rest.controller.storeSettings.adapter.StoreSettingsResponseAdapter
import com.rest.controller.storeSettings.data.StoreSettingsResponse
import com.rest.controller.storeSettings.data.StoreSettingsUpdateRequest
import com.service.storeSettings.StoreSettingsService
import com.service.storeSettings.dto.UpdateStoreSettingsDto
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stores/{storeId}/settings")
class StoreSettingsController(private val storeSettingsService: StoreSettingsService) {

    @GetMapping
    fun get(@PathVariable storeId: Long): StoreSettingsResponse =
        with(StoreSettingsResponseAdapter) { storeSettingsService.findByStore(storeId).toApiModel() }

    @PutMapping
    fun update(@PathVariable storeId: Long, @Valid @RequestBody request: StoreSettingsUpdateRequest): StoreSettingsResponse {
        val dto = UpdateStoreSettingsDto(
            requestsPerSecond = request.requestsPerSecond,
            burstLimit = request.burstLimit,
            allowedLanguages = request.allowedLanguages
        )
        return with(StoreSettingsResponseAdapter) { storeSettingsService.update(storeId, dto).toApiModel() }
    }
}
