package com.data.repository

import com.data.entity.ApiKeyStatus
import com.data.entity.StoreApiKey
import org.springframework.data.jpa.repository.JpaRepository

interface StoreApiKeyRepository : JpaRepository<StoreApiKey, Long> {
    fun findByStoreId(storeId: Long): List<StoreApiKey>
    fun findByKeyPrefixAndStatus(keyPrefix: String, status: ApiKeyStatus): StoreApiKey?
}
