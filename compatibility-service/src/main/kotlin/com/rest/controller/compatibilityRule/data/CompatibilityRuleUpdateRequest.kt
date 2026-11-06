package com.rest.controller.compatibilityRule.data

import com.data.entity.Severity

data class CompatibilityRuleUpdateRequest(
    val parameters: String? = null,
    val severity: Severity? = null,
    val enabled: Boolean? = null
)
