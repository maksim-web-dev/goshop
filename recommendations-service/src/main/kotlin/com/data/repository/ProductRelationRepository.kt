package com.data.repository

import com.data.entity.ProductRelation
import com.data.entity.RelationType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProductRelationRepository : JpaRepository<ProductRelation, UUID> {
    fun findBySourceProductIdAndRelationTypeOrderByScoreDesc(sourceProductId: UUID, relationType: RelationType): List<ProductRelation>
    fun findBySourceProductIdOrderByScoreDesc(sourceProductId: UUID): List<ProductRelation>
}
