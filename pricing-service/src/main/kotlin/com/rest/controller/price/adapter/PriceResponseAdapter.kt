package com.rest.controller.price.adapter

import com.data.entity.Price
import com.rest.controller.price.data.PriceResponse
import com.rest.util.ApiTypeAdapter

object PriceResponseAdapter : ApiTypeAdapter<PriceResponse, Price>() {
    override fun Price.toApiModel() = PriceResponse(
        id = id, productId = productId, storeId = storeId, amount = amount,
        currencyCode = currencyCode, validFrom = validFrom, validTo = validTo, updatedAt = updatedAt
    )
}
