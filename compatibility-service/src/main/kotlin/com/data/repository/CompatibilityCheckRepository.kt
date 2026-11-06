package com.data.repository

import com.data.entity.CompatibilityCheck
import org.springframework.data.jpa.repository.JpaRepository

interface CompatibilityCheckRepository : JpaRepository<CompatibilityCheck, Long>
