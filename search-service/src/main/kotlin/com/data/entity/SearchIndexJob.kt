package com.data.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "search_index_jobs")
class SearchIndexJob(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val operation: IndexOperation,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: IndexJobStatus = IndexJobStatus.PENDING,

    @Column(nullable = false)
    var attempts: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)