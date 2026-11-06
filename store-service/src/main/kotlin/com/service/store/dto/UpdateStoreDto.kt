package com.service.store.dto

import com.data.entity.StoreStatus

data class UpdateStoreDto(
    val name: String? = null,
    val defaultLanguage: String? = null,
    val defaultCurrency: String? = null,
    val status: StoreStatus? = null
)
