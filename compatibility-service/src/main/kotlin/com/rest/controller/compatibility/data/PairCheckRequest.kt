package com.rest.controller.compatibility.data

import java.util.UUID

data class PairCheckRequest(
    val sourceProductId: UUID,
    val targetProductId: UUID
)
