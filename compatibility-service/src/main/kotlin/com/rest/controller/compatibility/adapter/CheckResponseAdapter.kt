package com.rest.controller.compatibility.adapter

import com.rest.controller.compatibility.data.CheckResponse
import com.rest.controller.compatibility.data.CompatibilityIssueResponse
import com.rest.util.ApiTypeAdapter
import com.service.compatibilityCheck.CompatibilityResult

object CheckResponseAdapter : ApiTypeAdapter<CheckResponse, CompatibilityResult>() {
    override fun CompatibilityResult.toApiModel() = CheckResponse(
        compatible = compatible,
        issues = issues.map {
            CompatibilityIssueResponse(
                ruleId = it.rule.id,
                severity = it.severity,
                messageCode = it.messageCode
            )
        }
    )
}
