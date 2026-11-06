package com.service.compatibilityCheck

import com.data.entity.CompatibilityCheck
import com.data.entity.CompatibilityIssue
import com.data.entity.Severity
import com.data.repository.CompatibilityCheckRepository
import com.data.repository.CompatibilityIssueRepository
import com.data.repository.CompatibilityRuleRepository
import com.service.compatibilityCheck.dto.CheckCompatibilityDto
import com.service.compatibilityCheck.dto.PairCheckDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

data class CompatibilityResult(
    val compatible: Boolean,
    val issues: List<CompatibilityIssue>
)

@Service
class CompatibilityCheckService(
    private val compatibilityRuleRepository: CompatibilityRuleRepository,
    private val compatibilityCheckRepository: CompatibilityCheckRepository,
    private val compatibilityIssueRepository: CompatibilityIssueRepository
) {

    @Transactional
    fun check(dto: CheckCompatibilityDto): CompatibilityResult {
        val rules = compatibilityRuleRepository.findByEnabledTrue()
        val check = compatibilityCheckRepository.save(
            CompatibilityCheck(compatible = true, productIds = dto.productIds.toMutableList())
        )
        val issues = rules.mapNotNull { rule ->
            runCatching { evaluateRule(rule.id, dto.productIds) }.getOrNull()
                ?.let { messageCode ->
                    compatibilityIssueRepository.save(
                        CompatibilityIssue(
                            check = check,
                            rule = rule,
                            severity = rule.severity,
                            messageCode = messageCode
                        )
                    )
                }
        }
        val hasErrors = issues.any { it.severity == Severity.ERROR }
        if (hasErrors) {
            compatibilityCheckRepository.save(
                CompatibilityCheck(id = check.id, compatible = false, productIds = check.productIds, checkedAt = check.checkedAt)
            )
        }
        return CompatibilityResult(compatible = !hasErrors, issues = issues)
    }

    @Transactional
    fun checkPair(dto: PairCheckDto): CompatibilityResult =
        check(CheckCompatibilityDto(productIds = listOf(dto.sourceProductId, dto.targetProductId)))

    fun findCompatibleProducts(productId: UUID): List<UUID> {
        // Returns product IDs that passed compatibility checks with the given product.
        // Currently returns empty list — full implementation requires product catalog integration.
        return emptyList()
    }

    private fun evaluateRule(ruleId: UUID, productIds: List<UUID>): String? {
        // Stub: each concrete RuleType handler returns a messageCode on violation, null on pass.
        return null
    }
}
