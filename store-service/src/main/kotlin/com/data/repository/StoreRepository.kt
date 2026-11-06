package com.data.repository

import com.data.entity.Store
import com.data.entity.StoreStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StoreRepository : JpaRepository<Store, UUID> {
    fun findByDomain(domain: String): Store?
    fun findByStatus(status: StoreStatus): List<Store>
}
