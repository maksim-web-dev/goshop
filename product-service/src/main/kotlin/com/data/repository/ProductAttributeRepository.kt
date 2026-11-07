package com.data.repository

import com.data.entity.ProductAttribute
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface ProductAttributeRepository : JpaRepository<ProductAttribute, Long> {
    fun findByProductId(productId: Long): List<ProductAttribute>

    @Query("SELECT pa FROM ProductAttribute pa JOIN FETCH pa.categoryParameter WHERE pa.product.id = :productId")
    fun findByProductIdWithParameter(productId: Long): List<ProductAttribute>

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ProductAttribute pa WHERE pa.product.id = :productId")
    fun deleteByProductId(productId: Long)
}