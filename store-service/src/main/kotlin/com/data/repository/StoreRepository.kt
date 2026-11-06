package com.data.repository

import com.data.entity.Store
import com.data.entity.StoreStatus
import org.springframework.data.jpa.repository.JpaRepository

interface StoreRepository : JpaRepository<Store, Long> {
    fun findByDomain(domain: String): Store?
    fun findByStatus(status: StoreStatus): List<Store>
}
