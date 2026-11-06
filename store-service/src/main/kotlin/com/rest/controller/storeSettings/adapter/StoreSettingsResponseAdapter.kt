package com.rest.controller.storeSettings.adapter

import com.data.entity.StoreSettings
import com.rest.controller.storeSettings.data.StoreSettingsResponse
import com.rest.util.ApiTypeAdapter

object StoreSettingsResponseAdapter : ApiTypeAdapter<StoreSettingsResponse, StoreSettings>() {
    override fun StoreSettings.toApiModel() = StoreSettingsResponse(
        id = id, storeId = store.id, requestsPerSecond = requestsPerSecond,
        burstLimit = burstLimit, allowedLanguages = allowedLanguages.toList()
    )
}
