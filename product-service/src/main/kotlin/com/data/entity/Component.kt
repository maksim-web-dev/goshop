package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "components")
class Component(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_group_id", nullable = false)
    val componentGroup: ComponentGroup
)