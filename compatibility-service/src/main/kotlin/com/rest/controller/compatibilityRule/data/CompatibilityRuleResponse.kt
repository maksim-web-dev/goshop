package com.rest.controller.compatibilityRule.data

import com.data.entity.RuleType
import com.data.entity.Severity
import java.util.UUID

data class CompatibilityRuleResponse(
    val id: UUID,
    val code: String,
    val sourceProductTypeId: UUID,
    val targetProductTypeId: UUID,
    val ruleType: RuleType,
    val parameters: String?,
    val severity: Severity,
    val enabled: Boolean,
    val version: Int
)
