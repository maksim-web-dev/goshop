package com.rest.controller.compatibilityRule.adapter

import com.rest.controller.compatibilityRule.data.CompatibilityRuleCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.compatibilityRule.dto.CreateCompatibilityRuleDto

object CompatibilityRuleCreateDtoAdapter : ApiTypeAdapter<CompatibilityRuleCreateRequest, CreateCompatibilityRuleDto>() {
    override fun CompatibilityRuleCreateRequest.toServiceModel() = CreateCompatibilityRuleDto(
        code = code,
        sourceProductTypeId = sourceProductTypeId,
        targetProductTypeId = targetProductTypeId,
        ruleType = ruleType,
        parameters = parameters,
        severity = severity,
        enabled = enabled
    )
}
