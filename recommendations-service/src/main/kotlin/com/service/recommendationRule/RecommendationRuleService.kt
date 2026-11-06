package com.service.recommendationRule

import com.data.entity.RecommendationRule
import com.data.repository.RecommendationRuleRepository
import com.service.recommendationRule.dto.CreateRecommendationRuleDto
import com.service.recommendationRule.dto.UpdateRecommendationRuleDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class RecommendationRuleService(private val ruleRepository: RecommendationRuleRepository) {

    fun findAll(): List<RecommendationRule> = ruleRepository.findAll()

    fun findById(id: UUID): RecommendationRule =
        ruleRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Rule $id not found") }

    @Transactional
    fun create(dto: CreateRecommendationRuleDto): RecommendationRule =
        ruleRepository.save(
            RecommendationRule(
                name = dto.name,
                strategy = dto.strategy,
                parameters = dto.parameters,
                priority = dto.priority,
                enabled = dto.enabled
            )
        )

    @Transactional
    fun update(id: UUID, dto: UpdateRecommendationRuleDto): RecommendationRule {
        val existing = findById(id)
        return ruleRepository.save(
            RecommendationRule(
                id = existing.id,
                name = dto.name ?: existing.name,
                strategy = existing.strategy,
                parameters = dto.parameters ?: existing.parameters,
                priority = dto.priority ?: existing.priority,
                enabled = dto.enabled ?: existing.enabled
            )
        )
    }
}
