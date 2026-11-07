package com.data.entity

import jakarta.persistence.*
import org.hibernate.annotations.BatchSize

@Entity
@Table(name = "product_types")
@BatchSize(size = 25)
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