package com.service.recommendation

import com.data.entity.RelationType
import com.data.entity.RecommendationStrategy
import com.data.entity.RecommendationSnapshot
import com.data.repository.ProductRelationRepository
import com.data.repository.RecommendationSnapshotRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RecommendationService(
    private val productRelationRepository: ProductRelationRepository,
    private val snapshotRepository: RecommendationSnapshotRepository
) {

    fun findAll(productId: UUID): List<UUID> =
        productRelationRepository.findBySourceProductIdOrderByScoreDesc(productId)
            .map { it.targetProductId }

    fun findSimilar(productId: UUID): List<UUID> =
        findByType(productId, RelationType.SIMILAR)

    fun findAlternatives(productId: UUID): List<UUID> =
        findByType(productId, RelationType.ALTERNATIVE)

    fun findAccessories(productId: UUID): List<UUID> =
        findByType(productId, RelationType.ACCESSORY)

    fun buildForPc(productIds: List<UUID>): Map<UUID, List<UUID>> =
        productIds.associateWith { findAll(it) }

    @Transactional
    fun generateSnapshot(productId: UUID, strategy: RecommendationStrategy): RecommendationSnapshot {
        val recommended = when (strategy) {
            RecommendationStrategy.SIMILAR_ATTRIBUTES -> findByType(productId, RelationType.SIMILAR)
            RecommendationStrategy.SAME_CATEGORY -> findAll(productId)
            else -> findAll(productId)
        }
        val existing = snapshotRepository.findByProductIdAndStrategy(productId, strategy)
        return snapshotRepository.save(
            RecommendationSnapshot(
                id = existing?.id ?: java.util.UUID.randomUUID(),
                productId = productId,
                strategy = strategy,
                recommendedProductIds = recommended.toMutableList()
            )
        )
    }

    private fun findByType(productId: UUID, type: RelationType): List<UUID> =
        productRelationRepository.findBySourceProductIdAndRelationTypeOrderByScoreDesc(productId, type)
            .map { it.targetProductId }
}
