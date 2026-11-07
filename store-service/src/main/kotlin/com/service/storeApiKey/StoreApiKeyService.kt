package com.service.storeApiKey

import com.data.entity.ApiKeyStatus
import com.data.entity.StoreApiKey
import com.data.repository.StoreApiKeyRepository
import com.data.repository.StoreRepository
import com.data.repository.StoreSettingsRepository
import com.rest.controller.internal.data.ApiKeyValidateResponse
import com.service.storeApiKey.dto.CreateStoreApiKeyDto
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.SecureRandom
import java.time.Instant
import java.util.HexFormat

data class CreatedApiKey(val entity: StoreApiKey, val plainKey: String)

@Service
class StoreApiKeyService(
    private val storeApiKeyRepository: StoreApiKeyRepository,
    private val storeRepository: StoreRepository,
    private val storeSettingsRepository: StoreSettingsRepository
) {
    private val encoder = BCryptPasswordEncoder()
    private val rng = SecureRandom()

    fun findByStore(storeId: Long): List<StoreApiKey> =
        storeApiKeyRepository.findByStoreId(storeId)

    @Transactional
    fun create(storeId: Long, dto: CreateStoreApiKeyDto): CreatedApiKey {
        val store = storeRepository.findById(storeId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Store $storeId not found") }
        val rawBytes = ByteArray(32).also { rng.nextBytes(it) }
        val plainKey = "sk_live_" + HexFormat.of().formatHex(rawBytes)
        val prefix = plainKey.take(16)
        return CreatedApiKey(
            entity = storeApiKeyRepository.save(
                StoreApiKey(
                    store = store,
                    keyHash = encoder.encode(plainKey),
                    keyPrefix = prefix,
                    expiresAt = dto.expiresAt
                )
            ),
            plainKey = plainKey
        )
    }

    @Transactional(readOnly = true)
    fun validate(plainKey: String): ApiKeyValidateResponse {
        val prefix = plainKey.take(16)
        val key = storeApiKeyRepository.findByKeyPrefixAndStatus(prefix, ApiKeyStatus.ACTIVE)
            ?: return ApiKeyValidateResponse(valid = false)
        if (key.expiresAt != null && key.expiresAt.isBefore(Instant.now()))
            return ApiKeyValidateResponse(valid = false)
        if (!encoder.matches(plainKey, key.keyHash))
            return ApiKeyValidateResponse(valid = false)
        val store = key.store
        val settings = storeSettingsRepository.findByStoreId(store.id!!)
        return ApiKeyValidateResponse(
            valid = true,
            storeId = store.id,
            storeStatus = store.status.name,
            defaultLanguage = store.defaultLanguage,
            defaultCurrency = store.defaultCurrency,
            requestsPerSecond = settings?.requestsPerSecond ?: 100,
            burstLimit = settings?.burstLimit ?: 200
        )
    }

    @Transactional
    fun revoke(storeId: Long, keyId: Long) {
        val key = storeApiKeyRepository.findById(keyId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Key $keyId not found") }
        if (key.store.id != storeId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Key does not belong to store $storeId")
        storeApiKeyRepository.save(
            StoreApiKey(
                id = key.id, store = key.store, keyHash = key.keyHash, keyPrefix = key.keyPrefix,
                status = ApiKeyStatus.REVOKED, createdAt = key.createdAt,
                expiresAt = key.expiresAt, lastUsedAt = key.lastUsedAt
            )
        )
    }
}
