package com.rest.controller.discount.adapter

import com.rest.controller.discount.data.DiscountCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.discount.dto.CreateDiscountDto

object DiscountCreateDtoAdapter : ApiTypeAdapter<DiscountCreateRequest, CreateDiscountDto>() {
    override fun DiscountCreateRequest.toServiceModel() = CreateDiscountDto(
        storeId = storeId, productId = productId, discountType = discountType,
        discountValue = discountValue, startsAt = startsAt, endsAt = endsAt
    )
}
