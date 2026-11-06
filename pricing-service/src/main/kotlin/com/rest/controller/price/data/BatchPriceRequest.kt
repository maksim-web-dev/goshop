package com.rest.controller.price.data

import java.util.UUID

data class BatchPriceRequest(
    val productIds: List<UUID>
)
