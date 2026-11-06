package com.rest.controller.discount.adapter

import com.rest.controller.discount.data.DiscountUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.discount.dto.UpdateDiscountDto

object DiscountUpdateDtoAdapter : ApiTypeAdapter<DiscountUpdateRequest, UpdateDiscountDto>() {
    override fun DiscountUpdateRequest.toServiceModel() = UpdateDiscountDto(
        discountValue = discountValue, startsAt = startsAt, endsAt = endsAt, status = status
    )
}
