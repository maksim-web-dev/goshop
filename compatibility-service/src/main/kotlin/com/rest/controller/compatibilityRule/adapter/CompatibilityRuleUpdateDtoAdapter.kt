package com.rest.controller.compatibilityRule.adapter

import com.rest.controller.compatibilityRule.data.CompatibilityRuleUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.compatibilityRule.dto.UpdateCompatibilityRuleDto

object CompatibilityRuleUpdateDtoAdapter : ApiTypeAdapter<CompatibilityRuleUpdateRequest, UpdateCompatibilityRuleDto>() {
    override fun CompatibilityRuleUpdateRequest.toServiceModel() = UpdateCompatibilityRuleDto(
        parameters = parameters,
        severity = severity,
        enabled = enabled
    )
}
