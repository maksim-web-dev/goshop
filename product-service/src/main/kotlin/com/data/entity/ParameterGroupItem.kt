package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "parameter_group_items")
class ParameterGroupItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parameter_group_id", nullable = false)
    val parameterGroup: ParameterGroup
)