package com.data.repository

import com.data.entity.StoreSettings
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StoreSettingsRepository : JpaRepository<StoreSettings, UUID> {
    fun findByStoreId(storeId: UUID): StoreSettings?
}
