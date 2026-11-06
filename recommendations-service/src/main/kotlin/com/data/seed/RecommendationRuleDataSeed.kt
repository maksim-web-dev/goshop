package com.data.seed

import com.config.props.SeedProps
import com.data.entity.RecommendationRule
import com.data.entity.RecommendationStrategy
import com.data.repository.RecommendationRuleRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
@Order(1)
class RecommendationRuleDataSeed(
    private val ruleRepository: RecommendationRuleRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (ruleRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/recommendations_recommendation_rule.json")
        val dtos: Array<RuleDto> = jacksonObjectMapper().readValue(json)
        ruleRepository.saveAll(dtos.map {
            RecommendationRule(
                name = it.name,
                strategy = RecommendationStrategy.valueOf(it.strategy),
                parameters = it.parameters,
                priority = it.priority,
                enabled = it.enabled
            )
        })
    }

    private data class RuleDto(
        @param:JsonProperty("name")       val name: String,
        @param:JsonProperty("strategy")   val strategy: String,
        @param:JsonProperty("parameters") val parameters: String? = null,
        @param:JsonProperty("priority")   val priority: Int = 0,
        @param:JsonProperty("enabled")    val enabled: Boolean = true
    )
}
