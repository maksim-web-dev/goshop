package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "compatibility_rules")
class CompatibilityRule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // Унікальний код правила, наприклад CPU_MOTHERBOARD_SOCKET
    @Column(nullable = false, unique = true)
    var code: String,

    // ID типу товару з product-service (крос-сервісне посилання, без @ManyToOne)
    @Column(name = "source_product_type_id", nullable = false)
    var sourceProductTypeId: Long,

    @Column(name = "target_product_type_id", nullable = false)
    var targetProductTypeId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    var ruleType: RuleType,

    // JSONB: параметри для конкретного обробника правила
    @Column(columnDefinition = "jsonb")
    var parameters: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var severity: Severity = Severity.ERROR,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false)
    var version: Int = 1
)