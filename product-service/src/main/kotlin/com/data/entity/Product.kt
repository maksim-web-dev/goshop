package com.data.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val sku: String,

    @Column(nullable = false)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    val brand: Brand,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_type_id", nullable = false)
    val productType: ProductType,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ProductStatus = ProductStatus.DRAFT,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)