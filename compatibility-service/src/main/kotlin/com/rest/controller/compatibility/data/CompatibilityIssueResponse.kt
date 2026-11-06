package com.rest.controller.compatibility.data

import com.data.entity.Severity

data class CompatibilityIssueResponse(
    val ruleId: Long?,
    val severity: Severity,
    val messageCode: String
)
