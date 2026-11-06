package com.rest.controller.store.adapter

import com.rest.controller.store.data.StoreUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.store.dto.UpdateStoreDto

object StoreUpdateDtoAdapter : ApiTypeAdapter<StoreUpdateRequest, UpdateStoreDto>() {
    override fun StoreUpdateRequest.toServiceModel() = UpdateStoreDto(
        name = name, defaultLanguage = defaultLanguage,
        defaultCurrency = defaultCurrency, status = status
    )
}
