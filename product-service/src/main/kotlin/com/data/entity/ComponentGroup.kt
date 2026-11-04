package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "component_groups")
class ComponentGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String
)