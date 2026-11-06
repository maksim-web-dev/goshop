package com.rest.controller.store.adapter

import com.rest.controller.store.data.StoreCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.store.dto.CreateStoreDto

object StoreCreateDtoAdapter : ApiTypeAdapter<StoreCreateRequest, CreateStoreDto>() {
    override fun StoreCreateRequest.toServiceModel() = CreateStoreDto(
        name = name, domain = domain, countryCode = countryCode,
        defaultLanguage = defaultLanguage, defaultCurrency = defaultCurrency
    )
}
