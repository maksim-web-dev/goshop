package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "category_parameters")
class CategoryParameter(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(name = "code")
    val code: String? = null,

    @Column(name = "sort_order", nullable = false)
    val order: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,

    @Enumerated(EnumType.STRING)
    @Column(name = "parameter_type", nullable = false)
    val parameterType: ParameterType,

    // INT:     ціле значення
    // DOUBLE_2: ціла частина (дробова у doubleValue2)
    // DOUBLE_3: ціла частина (дробова у doubleValue3)
    @Column(name = "int_value")
    val intValue: Int? = null,

    @Column(name = "double_value2")
    val doubleValue2: Int? = null,

    @Column(name = "double_value3")
    val doubleValue3: Int? = null,

    @Column(name = "boolean_value")
    val booleanValue: Boolean? = null,

    @Column(name = "string_value")
    val stringValue: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_group_id")
    val parameterGroup: ParameterGroup? = null
)