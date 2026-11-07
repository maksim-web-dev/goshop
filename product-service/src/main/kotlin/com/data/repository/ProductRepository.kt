package com.data.repository

import com.data.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    fun existsBySku(sku: String): Boolean
    fun existsByCategoryId(categoryId: Long): Boolean

    @Query("SELECT p FROM Product p JOIN FETCH p.brand JOIN FETCH p.category JOIN FETCH p.productType WHERE p.id IN :ids")
    fun findAllWithRelationsByIds(ids: Collection<Long>): List<Product>

    @Query("SELECT p FROM Product p JOIN FETCH p.brand JOIN FETCH p.category JOIN FETCH p.productType WHERE p.id = :id")
    fun findByIdWithRelations(id: Long): Optional<Product>
}