package com.rest.controller.compatibilityRule.adapter

import com.data.entity.CompatibilityRule
import com.rest.controller.compatibilityRule.data.CompatibilityRuleResponse
import com.rest.util.ApiTypeAdapter

object CompatibilityRuleResponseAdapter : ApiTypeAdapter<CompatibilityRuleResponse, CompatibilityRule>() {
    override fun CompatibilityRule.toApiModel() = CompatibilityRuleResponse(
        id = id,
        code = code,
        sourceProductTypeId = sourceProductTypeId,
        targetProductTypeId = targetProductTypeId,
        ruleType = ruleType,
        parameters = parameters,
        severity = severity,
        enabled = enabled,
        version = version
    )
}
