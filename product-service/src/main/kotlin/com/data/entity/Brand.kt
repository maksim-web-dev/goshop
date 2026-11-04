package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "brands")
class Brand(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(name = "website_url")
    val websiteUrl: String? = null
)