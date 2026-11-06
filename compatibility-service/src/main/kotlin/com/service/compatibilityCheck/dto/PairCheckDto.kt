package com.service.compatibilityCheck.dto

import java.util.UUID

data class PairCheckDto(
    val sourceProductId: UUID,
    val targetProductId: UUID
)
