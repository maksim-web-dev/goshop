package com.data.repository

import com.data.entity.ParameterGroup
import org.springframework.data.jpa.repository.JpaRepository

interface ParameterGroupRepository : JpaRepository<ParameterGroup, Long>