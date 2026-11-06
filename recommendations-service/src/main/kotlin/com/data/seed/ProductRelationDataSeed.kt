package com.data.seed

import com.config.props.SeedProps
import com.data.entity.ProductRelation
import com.data.entity.RelationType
import com.data.repository.ProductRelationRepository
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
@Order(2)
class ProductRelationDataSeed(
    private val productRelationRepository: ProductRelationRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (productRelationRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_relation.json")
        val dtos: Array<RelationDto> = jacksonObjectMapper().readValue(json)
        productRelationRepository.saveAll(dtos.map {
            ProductRelation(
                sourceProductId = UUID.fromString(it.sourceProductId),
                targetProductId = UUID.fromString(it.targetProductId),
                relationType = RelationType.valueOf(it.relationType),
                score = it.score
            )
        })
    }

    private data class RelationDto(
        @param:JsonProperty("source_product_id") val sourceProductId: String,
        @param:JsonProperty("target_product_id") val targetProductId: String,
        @param:JsonProperty("relation_type")     val relationType: String,
        @param:JsonProperty("score")             val score: Double = 0.0
    )
}
