package com.service.storeWebhook

import com.data.entity.StoreWebhook
import com.data.repository.StoreRepository
import com.data.repository.StoreWebhookRepository
import com.service.storeWebhook.dto.CreateStoreWebhookDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class StoreWebhookService(
    private val webhookRepository: StoreWebhookRepository,
    private val storeRepository: StoreRepository
) {

    fun findByStore(storeId: UUID): List<StoreWebhook> =
        webhookRepository.findByStoreId(storeId)

    @Transactional
    fun create(storeId: UUID, dto: CreateStoreWebhookDto): StoreWebhook {
        val store = storeRepository.findById(storeId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Store $storeId not found") }
        return webhookRepository.save(
            StoreWebhook(
                store = store,
                url = dto.url,
                eventTypes = dto.eventTypes.toMutableList(),
                secretRef = dto.secretRef,
                enabled = dto.enabled
            )
        )
    }
}
