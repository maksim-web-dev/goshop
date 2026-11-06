package com.rest.controller.price.adapter

import com.data.entity.PriceHistory
import com.rest.controller.price.data.PriceHistoryResponse
import com.rest.util.ApiTypeAdapter

object PriceHistoryResponseAdapter : ApiTypeAdapter<PriceHistoryResponse, PriceHistory>() {
    override fun PriceHistory.toApiModel() = PriceHistoryResponse(
        id = id, priceId = price.id, oldAmount = oldAmount, newAmount = newAmount, changedAt = changedAt
    )
}
