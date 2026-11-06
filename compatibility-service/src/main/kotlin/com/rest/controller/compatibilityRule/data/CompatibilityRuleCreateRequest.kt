package com.rest.controller.compatibilityRule.data

import com.data.entity.RuleType
import com.data.entity.Severity

data class CompatibilityRuleCreateRequest(
    val code: String,
    val sourceProductTypeId: Long,
    val targetProductTypeId: Long,
    val ruleType: RuleType,
    val parameters: String? = null,
    val severity: Severity = Severity.ERROR,
    val enabled: Boolean = true
)
