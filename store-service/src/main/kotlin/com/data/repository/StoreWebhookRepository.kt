package com.data.repository

import com.data.entity.StoreWebhook
import org.springframework.data.jpa.repository.JpaRepository

interface StoreWebhookRepository : JpaRepository<StoreWebhook, Long> {
    fun findByStoreId(storeId: Long): List<StoreWebhook>
}
