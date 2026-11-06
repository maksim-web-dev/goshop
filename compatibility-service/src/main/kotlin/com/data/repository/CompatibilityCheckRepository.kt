package com.data.repository

import com.data.entity.CompatibilityCheck
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompatibilityCheckRepository : JpaRepository<CompatibilityCheck, UUID>
