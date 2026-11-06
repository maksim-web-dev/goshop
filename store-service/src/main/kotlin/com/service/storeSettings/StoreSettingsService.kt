package com.service.storeSettings

import com.data.entity.StoreSettings
import com.data.repository.StoreRepository
import com.data.repository.StoreSettingsRepository
import com.service.storeSettings.dto.UpdateStoreSettingsDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class StoreSettingsService(
    private val settingsRepository: StoreSettingsRepository,
    private val storeRepository: StoreRepository
) {

    fun findByStore(storeId: Long): StoreSettings =
        settingsRepository.findByStoreId(storeId)
            ?: run {
                val store = storeRepository.findById(storeId)
                    .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Store $storeId not found") }
                settingsRepository.save(StoreSettings(store = store))
            }

    @Transactional
    fun update(storeId: Long, dto: UpdateStoreSettingsDto): StoreSettings {
        val existing = findByStore(storeId)
        return settingsRepository.save(
            StoreSettings(
                id = existing.id,
                store = existing.store,
                requestsPerSecond = dto.requestsPerSecond ?: existing.requestsPerSecond,
                burstLimit = dto.burstLimit ?: existing.burstLimit,
                allowedLanguages = (dto.allowedLanguages ?: existing.allowedLanguages).toMutableList()
            )
        )
    }
}
