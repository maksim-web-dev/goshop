package com.data.repository

import com.data.entity.Component
import org.springframework.data.jpa.repository.JpaRepository

interface ComponentRepository : JpaRepository<Component, Long> {
    fun findByComponentGroupId(componentGroupId: Long): List<Component>
}