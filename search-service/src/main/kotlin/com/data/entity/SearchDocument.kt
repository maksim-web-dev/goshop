package com.data.entity

import java.time.Instant
import java.util.UUID

// Не JPA entity — пошукова проекція каталогу (PostgreSQL FTS або OpenSearch документ)
data class SearchDocument(
    val productId: UUID,
    val sku: String,
    val name: String,
    // JSONB: { "uk": "Відеокарта", "en": "GPU" }
    val localizedNames: Map<String, String> = emptyMap(),
    val brand: String,
    val categoryId: UUID,
    // JSONB: список атрибутів для фільтрації
    val attributes: Map<String, String> = emptyMap(),
    val searchableText: String,
    val status: String,
    val indexedAt: Instant = Instant.now()
)