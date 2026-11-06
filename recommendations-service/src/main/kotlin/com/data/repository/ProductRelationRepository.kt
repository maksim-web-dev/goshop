package com.data.repository

import com.data.entity.ProductRelation
import com.data.entity.RelationType
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRelationRepository : JpaRepository<ProductRelation, Long> {
    fun findBySourceProductIdAndRelationTypeOrderByScoreDesc(sourceProductId: Long, relationType: RelationType): List<ProductRelation>
    fun findBySourceProductIdOrderByScoreDesc(sourceProductId: Long): List<ProductRelation>
}
