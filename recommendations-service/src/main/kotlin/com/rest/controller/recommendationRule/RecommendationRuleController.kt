package com.rest.controller.recommendationRule

import com.rest.controller.recommendationRule.adapter.RecommendationRuleCreateDtoAdapter
import com.rest.controller.recommendationRule.adapter.RecommendationRuleResponseAdapter
import com.rest.controller.recommendationRule.adapter.RecommendationRuleUpdateDtoAdapter
import com.rest.controller.recommendationRule.data.RecommendationRuleCreateRequest
import com.rest.controller.recommendationRule.data.RecommendationRuleResponse
import com.rest.controller.recommendationRule.data.RecommendationRuleUpdateRequest
import com.service.recommendationRule.RecommendationRuleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/recommendations/rules")
class RecommendationRuleController(private val ruleService: RecommendationRuleService) {

    @GetMapping
    fun getAll(): List<RecommendationRuleResponse> =
        with(RecommendationRuleResponseAdapter) { ruleService.findAll().toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: RecommendationRuleCreateRequest): RecommendationRuleResponse {
        val dto = with(RecommendationRuleCreateDtoAdapter) { request.toServiceModel() }
        return with(RecommendationRuleResponseAdapter) { ruleService.create(dto).toApiModel() }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: RecommendationRuleUpdateRequest): RecommendationRuleResponse {
        val dto = with(RecommendationRuleUpdateDtoAdapter) { request.toServiceModel() }
        return with(RecommendationRuleResponseAdapter) { ruleService.update(id, dto).toApiModel() }
    }
}
