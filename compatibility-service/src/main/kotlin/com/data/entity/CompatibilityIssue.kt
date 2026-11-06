package com.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "compatibility_issues")
class CompatibilityIssue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_id", nullable = false)
    var check: CompatibilityCheck,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    var rule: CompatibilityRule,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var severity: Severity,

    // i18n-ключ для повідомлення про проблему, наприклад SOCKET_MISMATCH
    @Column(name = "message_code", nullable = false)
    var messageCode: String
)