package com.data.seed

import com.config.props.SeedProps
import com.data.entity.CompatibilityRule
import com.data.entity.RuleType
import com.data.entity.Severity
import com.data.repository.CompatibilityRuleRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@Transactional
@Order(1)
class CompatibilityRuleDataSeed(
    private val compatibilityRuleRepository: CompatibilityRuleRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (compatibilityRuleRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/compatibility_rule.json")
        val dtos: Array<CompatibilityRuleDto> = jacksonObjectMapper().readValue(json)
        compatibilityRuleRepository.saveAll(dtos.map {
            CompatibilityRule(
                code = it.code,
                sourceProductTypeId = UUID.fromString(it.sourceProductTypeId),
                targetProductTypeId = UUID.fromString(it.targetProductTypeId),
                ruleType = RuleType.valueOf(it.ruleType),
                parameters = it.parameters,
                severity = Severity.valueOf(it.severity),
                enabled = it.enabled
            )
        })
    }

    private data class CompatibilityRuleDto(
        @param:JsonProperty("code")                    val code: String,
        @param:JsonProperty("source_product_type_id")  val sourceProductTypeId: String,
        @param:JsonProperty("target_product_type_id")  val targetProductTypeId: String,
        @param:JsonProperty("rule_type")               val ruleType: String,
        @param:JsonProperty("parameters")              val parameters: String? = null,
        @param:JsonProperty("severity")                val severity: String = "ERROR",
        @param:JsonProperty("enabled")                 val enabled: Boolean = true
    )
}
