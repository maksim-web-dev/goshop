package com.data.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "store_webhooks",
    indexes = [
        Index(name = "idx_store_webhooks_store_id", columnList = "store_id")
    ]
)
class StoreWebhook(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    val store: Store,

    @Column(nullable = false)
    val url: String,

    // Події, на які підписаний webhook: ["product.created", "price.updated"]
    @ElementCollection
    @CollectionTable(
        name = "store_webhook_event_types",
        joinColumns = [JoinColumn(name = "webhook_id")]
    )
    @Column(name = "event_type", nullable = false)
    val eventTypes: MutableList<String> = mutableListOf(),

    // Посилання на секрет у Vault/Secrets Manager — не зберігаємо сам секрет
    @Column(name = "secret_ref", nullable = false)
    val secretRef: String,

    @Column(nullable = false)
    val enabled: Boolean = true
)