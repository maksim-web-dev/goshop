package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "product_types")
class ProductType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,

    @Column(nullable = false)
    val name: String
)