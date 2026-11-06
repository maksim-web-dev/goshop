package com.rest.controller.store.data

import com.data.entity.StoreStatus

data class StoreUpdateRequest(
    val name: String? = null,
    val defaultLanguage: String? = null,
    val defaultCurrency: String? = null,
    val status: StoreStatus? = null
)
