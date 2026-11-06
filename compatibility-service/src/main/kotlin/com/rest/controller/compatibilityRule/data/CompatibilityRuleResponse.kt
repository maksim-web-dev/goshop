package com.rest.controller.compatibilityRule.data

import com.data.entity.RuleType
import com.data.entity.Severity

data class CompatibilityRuleResponse(
    val id: Long?,
    val code: String,
    val sourceProductTypeId: Long,
    val targetProductTypeId: Long,
    val ruleType: RuleType,
    val parameters: String?,
    val severity: Severity,
    val enabled: Boolean,
    val version: Int
)
