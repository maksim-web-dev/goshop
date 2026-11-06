package com.service.compatibilityRule

import com.data.entity.CompatibilityRule
import com.data.repository.CompatibilityRuleRepository
import com.service.compatibilityRule.dto.CreateCompatibilityRuleDto
import com.service.compatibilityRule.dto.UpdateCompatibilityRuleDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CompatibilityRuleService(
    private val compatibilityRuleRepository: CompatibilityRuleRepository
) {

    fun findAll(): List<CompatibilityRule> = compatibilityRuleRepository.findAll()

    fun findById(id: Long): CompatibilityRule =
        compatibilityRuleRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Rule $id not found") }

    @Transactional
    fun create(dto: CreateCompatibilityRuleDto): CompatibilityRule {
        if (compatibilityRuleRepository.existsByCode(dto.code))
            throw ResponseStatusException(HttpStatus.CONFLICT, "Rule with code '${dto.code}' already exists")
        return compatibilityRuleRepository.save(
            CompatibilityRule(
                code = dto.code,
                sourceProductTypeId = dto.sourceProductTypeId,
                targetProductTypeId = dto.targetProductTypeId,
                ruleType = dto.ruleType,
                parameters = dto.parameters,
                severity = dto.severity,
                enabled = dto.enabled
            )
        )
    }

    @Transactional
    fun update(id: Long, dto: UpdateCompatibilityRuleDto): CompatibilityRule {
        val existing = findById(id)
        return compatibilityRuleRepository.save(
            CompatibilityRule(
                id = existing.id,
                code = existing.code,
                sourceProductTypeId = existing.sourceProductTypeId,
                targetProductTypeId = existing.targetProductTypeId,
                ruleType = existing.ruleType,
                parameters = dto.parameters ?: existing.parameters,
                severity = dto.severity ?: existing.severity,
                enabled = dto.enabled ?: existing.enabled,
                version = existing.version + 1
            )
        )
    }

    @Transactional
    fun disable(id: Long) {
        val existing = findById(id)
        compatibilityRuleRepository.save(
            CompatibilityRule(
                id = existing.id,
                code = existing.code,
                sourceProductTypeId = existing.sourceProductTypeId,
                targetProductTypeId = existing.targetProductTypeId,
                ruleType = existing.ruleType,
                parameters = existing.parameters,
                severity = existing.severity,
                enabled = false,
                version = existing.version + 1
            )
        )
    }
}
