package com.data.repository

import com.data.entity.ProductRelation
import com.data.entity.RelationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProductRelationRepository : JpaRepository<ProductRelation, Long> {
    fun findBySourceProductIdAndRelationTypeOrderByScoreDesc(sourceProductId: Long, relationType: RelationType): List<ProductRelation>
    fun findBySourceProductIdOrderByScoreDesc(sourceProductId: Long): List<ProductRelation>

    @Query("SELECT pr FROM ProductRelation pr WHERE pr.sourceProductId IN :ids ORDER BY pr.sourceProductId ASC, pr.score DESC")
    fun findBySourceProductIdIn(ids: Collection<Long>): List<ProductRelation>
}
