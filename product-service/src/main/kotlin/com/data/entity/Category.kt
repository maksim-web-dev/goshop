package com.data.entity

import jakarta.persistence.*
import org.hibernate.annotations.BatchSize

@Entity
@Table(name = "categories")
@BatchSize(size = 25)
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    val parent: Category? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val slug: String
)