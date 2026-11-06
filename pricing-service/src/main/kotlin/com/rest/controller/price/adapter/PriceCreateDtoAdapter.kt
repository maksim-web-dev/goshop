package com.rest.controller.price.adapter

import com.rest.controller.price.data.PriceCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.price.dto.CreatePriceDto

object PriceCreateDtoAdapter : ApiTypeAdapter<PriceCreateRequest, CreatePriceDto>() {
    override fun PriceCreateRequest.toServiceModel() = CreatePriceDto(
        productId = productId, storeId = storeId, amount = amount,
        currencyCode = currencyCode, validFrom = validFrom, validTo = validTo
    )
}
