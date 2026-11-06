package com.rest.controller.compatibilityRule

import com.rest.controller.compatibilityRule.adapter.CompatibilityRuleCreateDtoAdapter
import com.rest.controller.compatibilityRule.adapter.CompatibilityRuleResponseAdapter
import com.rest.controller.compatibilityRule.adapter.CompatibilityRuleUpdateDtoAdapter
import com.rest.controller.compatibilityRule.data.CompatibilityRuleCreateRequest
import com.rest.controller.compatibilityRule.data.CompatibilityRuleResponse
import com.rest.controller.compatibilityRule.data.CompatibilityRuleUpdateRequest
import com.service.compatibilityRule.CompatibilityRuleService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/compatibility/rules")
class CompatibilityRuleController(private val compatibilityRuleService: CompatibilityRuleService) {

    @GetMapping
    fun getAll(): List<CompatibilityRuleResponse> =
        with(CompatibilityRuleResponseAdapter) { compatibilityRuleService.findAll().toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CompatibilityRuleCreateRequest): CompatibilityRuleResponse {
        val dto = with(CompatibilityRuleCreateDtoAdapter) { request.toServiceModel() }
        return with(CompatibilityRuleResponseAdapter) { compatibilityRuleService.create(dto).toApiModel() }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody request: CompatibilityRuleUpdateRequest): CompatibilityRuleResponse {
        val dto = with(CompatibilityRuleUpdateDtoAdapter) { request.toServiceModel() }
        return with(CompatibilityRuleResponseAdapter) { compatibilityRuleService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun disable(@PathVariable id: UUID) = compatibilityRuleService.disable(id)
}
