package com.rest.controller.searchIndex.data

import java.util.UUID

data class ReindexRequest(
    val productIds: List<UUID>
)
