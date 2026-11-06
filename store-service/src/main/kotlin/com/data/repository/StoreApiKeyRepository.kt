package com.data.repository

import com.data.entity.ApiKeyStatus
import com.data.entity.StoreApiKey
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StoreApiKeyRepository : JpaRepository<StoreApiKey, UUID> {
    fun findByStoreId(storeId: UUID): List<StoreApiKey>
    fun findByKeyPrefixAndStatus(keyPrefix: String, status: ApiKeyStatus): StoreApiKey?
}
