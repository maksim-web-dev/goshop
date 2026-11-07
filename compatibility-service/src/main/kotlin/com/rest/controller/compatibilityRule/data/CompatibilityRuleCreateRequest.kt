package com.rest.controller.compatibilityRule.data

import com.data.entity.RuleType
import com.data.entity.Severity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CompatibilityRuleCreateRequest(
    @field:NotBlank @field:Size(max = 100)
    val code: String,
    val sourceProductTypeId: Long,
    val targetProductTypeId: Long,
    val ruleType: RuleType,
    val parameters: String? = null,
    val severity: Severity = Severity.ERROR,
    val enabled: Boolean = true
)
