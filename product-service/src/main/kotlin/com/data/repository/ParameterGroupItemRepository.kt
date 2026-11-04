package com.data.repository

import com.data.entity.ParameterGroupItem
import org.springframework.data.jpa.repository.JpaRepository

interface ParameterGroupItemRepository : JpaRepository<ParameterGroupItem, Long> {
    fun findByParameterGroupId(parameterGroupId: Long): List<ParameterGroupItem>
}