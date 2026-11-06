package com.data.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "translations",
    uniqueConstraints = [UniqueConstraint(columnNames = ["entity_type", "entity_id", "field_name", "language_code"])]
)
class Translation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    val entityType: EntityType,

    // UUID сутності з відповідного сервісу (крос-сервісне посилання)
    @Column(name = "entity_id", nullable = false)
    val entityId: UUID,

    // Назва поля, наприклад: name, description
    @Column(name = "field_name", nullable = false)
    val fieldName: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_code", nullable = false)
    val language: Language,

    @Column(name = "translated_value", nullable = false, columnDefinition = "TEXT")
    val translatedValue: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TranslationStatus = TranslationStatus.DRAFT,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)