package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "languages")
class Language(
    // BCP 47 код мови, наприклад: uk, en, de
    @Id
    val code: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val enabled: Boolean = true
)