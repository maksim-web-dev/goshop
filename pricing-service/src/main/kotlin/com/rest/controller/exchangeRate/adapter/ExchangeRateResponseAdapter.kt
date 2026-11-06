package com.rest.controller.exchangeRate.adapter

import com.data.entity.ExchangeRate
import com.rest.controller.exchangeRate.data.ExchangeRateResponse
import com.rest.util.ApiTypeAdapter

object ExchangeRateResponseAdapter : ApiTypeAdapter<ExchangeRateResponse, ExchangeRate>() {
    override fun ExchangeRate.toApiModel() = ExchangeRateResponse(
        id = id, fromCurrency = fromCurrency, toCurrency = toCurrency,
        rate = rate, effectiveAt = effectiveAt
    )
}
