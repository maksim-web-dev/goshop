package com.data.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "translation_jobs")
class TranslationJob(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    val entityType: EntityType,

    @Column(name = "entity_id", nullable = false)
    val entityId: UUID,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_language_code", nullable = false)
    val targetLanguage: Language,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TranslationJobStatus = TranslationJobStatus.PENDING,

    @Column(nullable = false)
    val attempts: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "completed_at")
    var completedAt: Instant? = null
)