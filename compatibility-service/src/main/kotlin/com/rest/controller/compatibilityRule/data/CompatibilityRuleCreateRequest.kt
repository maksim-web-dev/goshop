package com.rest.controller.compatibilityRule.data

import com.data.entity.RuleType
import com.data.entity.Severity
import java.util.UUID

data class CompatibilityRuleCreateRequest(
    val code: String,
    val sourceProductTypeId: UUID,
    val targetProductTypeId: UUID,
    val ruleType: RuleType,
    val parameters: String? = null,
    val severity: Severity = Severity.ERROR,
    val enabled: Boolean = true
)
