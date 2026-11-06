package com.service.store

import com.data.entity.Store
import com.data.entity.StoreStatus
import com.data.repository.StoreRepository
import com.service.store.dto.CreateStoreDto
import com.service.store.dto.UpdateStoreDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class StoreService(private val storeRepository: StoreRepository) {

    fun findAll(): List<Store> = storeRepository.findAll()

    fun findById(id: Long): Store =
        storeRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Store $id not found") }

    @Transactional
    fun create(dto: CreateStoreDto): Store {
        if (storeRepository.findByDomain(dto.domain) != null)
            throw ResponseStatusException(HttpStatus.CONFLICT, "Domain '${dto.domain}' already registered")
        return storeRepository.save(
            Store(
                name = dto.name,
                domain = dto.domain,
                countryCode = dto.countryCode,
                defaultLanguage = dto.defaultLanguage,
                defaultCurrency = dto.defaultCurrency
            )
        )
    }

    @Transactional
    fun update(id: Long, dto: UpdateStoreDto): Store {
        val existing = findById(id)
        return storeRepository.save(
            Store(
                id = existing.id,
                name = dto.name ?: existing.name,
                domain = existing.domain,
                countryCode = existing.countryCode,
                defaultLanguage = dto.defaultLanguage ?: existing.defaultLanguage,
                defaultCurrency = dto.defaultCurrency ?: existing.defaultCurrency,
                status = dto.status ?: existing.status,
                createdAt = existing.createdAt
            )
        )
    }

    @Transactional
    fun deactivate(id: Long) {
        val existing = findById(id)
        storeRepository.save(
            Store(
                id = existing.id, name = existing.name, domain = existing.domain,
                countryCode = existing.countryCode, defaultLanguage = existing.defaultLanguage,
                defaultCurrency = existing.defaultCurrency, status = StoreStatus.INACTIVE,
                createdAt = existing.createdAt
            )
        )
    }
}
