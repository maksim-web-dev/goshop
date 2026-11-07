package com.rest.controller.store.data

import com.data.entity.StoreStatus
import jakarta.validation.constraints.Size

data class StoreUpdateRequest(
    @field:Size(min = 1, max = 255)
    val name: String? = null,

    @field:Size(min = 2, max = 10)
    val defaultLanguage: String? = null,

    @field:Size(min = 3, max = 3)
    val defaultCurrency: String? = null,

    val status: StoreStatus? = null
)
