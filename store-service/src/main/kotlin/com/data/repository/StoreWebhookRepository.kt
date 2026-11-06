package com.data.repository

import com.data.entity.StoreWebhook
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StoreWebhookRepository : JpaRepository<StoreWebhook, UUID> {
    fun findByStoreId(storeId: UUID): List<StoreWebhook>
}
