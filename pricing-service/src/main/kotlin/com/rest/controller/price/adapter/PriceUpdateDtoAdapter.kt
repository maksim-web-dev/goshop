package com.rest.controller.price.adapter

import com.rest.controller.price.data.PriceUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.price.dto.UpdatePriceDto

object PriceUpdateDtoAdapter : ApiTypeAdapter<PriceUpdateRequest, UpdatePriceDto>() {
    override fun PriceUpdateRequest.toServiceModel() = UpdatePriceDto(
        amount = amount, currencyCode = currencyCode, validFrom = validFrom, validTo = validTo
    )
}
