package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "parameter_groups")
class ParameterGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String
)