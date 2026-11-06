package com.service.compatibilityCheck.dto

import java.util.UUID

data class CheckCompatibilityDto(
    val productIds: List<UUID>
)
