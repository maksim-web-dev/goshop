package com.rest.controller.discount.adapter

import com.data.entity.Discount
import com.rest.controller.discount.data.DiscountResponse
import com.rest.util.ApiTypeAdapter

object DiscountResponseAdapter : ApiTypeAdapter<DiscountResponse, Discount>() {
    override fun Discount.toApiModel() = DiscountResponse(
        id = id, storeId = storeId, productId = productId, discountType = discountType,
        discountValue = discountValue, startsAt = startsAt, endsAt = endsAt, status = status
    )
}
