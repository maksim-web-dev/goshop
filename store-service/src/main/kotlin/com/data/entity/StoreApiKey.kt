package com.data.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "store_api_keys",
    indexes = [
        Index(name = "idx_store_api_keys_store_id", columnList = "store_id"),
        Index(name = "idx_store_api_keys_key_prefix_status", columnList = "key_prefix, status")
    ]
)
class StoreApiKey(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    val store: Store,

    // bcrypt/argon2 хеш ключа — відкритий ключ не зберігаємо
    @Column(name = "key_hash", nullable = false)
    val keyHash: String,

    // Перші 8 символів ключа для відображення в UI: "sk_live_ab12****"
    @Column(name = "key_prefix", nullable = false, length = 16)
    val keyPrefix: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ApiKeyStatus = ApiKeyStatus.ACTIVE,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "expires_at")
    val expiresAt: Instant? = null,

    @Column(name = "last_used_at")
    var lastUsedAt: Instant? = null
)