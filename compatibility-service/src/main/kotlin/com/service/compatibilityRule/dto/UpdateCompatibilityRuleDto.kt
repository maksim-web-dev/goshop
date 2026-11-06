package com.service.compatibilityRule.dto

import com.data.entity.Severity

data class UpdateCompatibilityRuleDto(
    val parameters: String? = null,
    val severity: Severity? = null,
    val enabled: Boolean? = null
)
