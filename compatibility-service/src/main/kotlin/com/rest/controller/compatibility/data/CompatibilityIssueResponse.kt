package com.rest.controller.compatibility.data

import com.data.entity.Severity
import java.util.UUID

data class CompatibilityIssueResponse(
    val ruleId: UUID,
    val severity: Severity,
    val messageCode: String
)
