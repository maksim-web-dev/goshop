package com.data.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "product_attributes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["product_id", "category_parameter_id"])]
)
class ProductAttribute(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_parameter_id", nullable = false)
    val categoryParameter: CategoryParameter,

    // Текстове або JSONB значення; строга типізація — наступна версія
    @Column(nullable = false, columnDefinition = "TEXT")
    val value: String
)