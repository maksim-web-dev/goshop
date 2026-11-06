package com.service.recommendation

import com.data.entity.RelationType
import com.data.entity.RecommendationStrategy
import com.data.entity.RecommendationSnapshot
import com.data.repository.ProductRelationRepository
import com.data.repository.RecommendationSnapshotRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecommendationService(
    private val productRelationRepository: ProductRelationRepository,
    private val snapshotRepository: RecommendationSnapshotRepository
) {

    fun findAll(productId: Long): List<Long> =
        productRelationRepository.findBySourceProductIdOrderByScoreDesc(productId)
            .map { it.targetProductId }

    fun findSimilar(productId: Long): List<Long> =
        findByType(productId, RelationType.SIMILAR)

    fun findAlternatives(productId: Long): List<Long> =
        findByType(productId, RelationType.ALTERNATIVE)

    fun findAccessories(productId: Long): List<Long> =
        findByType(productId, RelationType.ACCESSORY)

    fun buildForPc(productIds: List<Long>): Map<Long, List<Long>> =
        productIds.associateWith { findAll(it) }

    @Transactional
    fun generateSnapshot(productId: Long, strategy: RecommendationStrategy): RecommendationSnapshot {
        val recommended = when (strategy) {
            RecommendationStrategy.SIMILAR_ATTRIBUTES -> findByType(productId, RelationType.SIMILAR)
            RecommendationStrategy.SAME_CATEGORY -> findAll(productId)
            else -> findAll(productId)
        }
        val existing = snapshotRepository.findByProductIdAndStrategy(productId, strategy)
        return snapshotRepository.save(
            RecommendationSnapshot(
                id = existing?.id,
                productId = productId,
                strategy = strategy,
                recommendedProductIds = recommended.toMutableList()
            )
        )
    }

    private fun findByType(productId: Long, type: RelationType): List<Long> =
        productRelationRepository.findBySourceProductIdAndRelationTypeOrderByScoreDesc(productId, type)
            .map { it.targetProductId }
}
