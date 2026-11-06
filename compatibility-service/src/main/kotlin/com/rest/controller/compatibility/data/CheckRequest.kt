package com.rest.controller.compatibility.data

import java.util.UUID

data class CheckRequest(
    val productIds: List<UUID>
)
