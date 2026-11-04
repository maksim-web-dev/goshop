package com.data.repository

import com.data.entity.ComponentGroup
import org.springframework.data.jpa.repository.JpaRepository

interface ComponentGroupRepository : JpaRepository<ComponentGroup, Long>