package com.rest.controller.store.adapter

import com.data.entity.Store
import com.rest.controller.store.data.StoreResponse
import com.rest.util.ApiTypeAdapter

object StoreResponseAdapter : ApiTypeAdapter<StoreResponse, Store>() {
    override fun Store.toApiModel() = StoreResponse(
        id = id, name = name, domain = domain, countryCode = countryCode,
        defaultLanguage = defaultLanguage, defaultCurrency = defaultCurrency,
        status = status, createdAt = createdAt
    )
}
