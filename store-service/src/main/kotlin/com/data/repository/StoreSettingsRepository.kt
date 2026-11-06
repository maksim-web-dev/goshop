package com.data.repository

import com.data.entity.StoreSettings
import org.springframework.data.jpa.repository.JpaRepository

interface StoreSettingsRepository : JpaRepository<StoreSettings, Long> {
    fun findByStoreId(storeId: Long): StoreSettings?
}
