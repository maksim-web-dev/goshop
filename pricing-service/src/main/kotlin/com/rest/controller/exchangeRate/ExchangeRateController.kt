package com.rest.controller.exchangeRate

import com.rest.controller.exchangeRate.adapter.ExchangeRateResponseAdapter
import com.rest.controller.exchangeRate.data.ExchangeRateResponse
import com.service.exchangeRate.ExchangeRateService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exchange-rates")
class ExchangeRateController(private val exchangeRateService: ExchangeRateService) {

    @GetMapping
    fun getAll(): List<ExchangeRateResponse> =
        with(ExchangeRateResponseAdapter) { exchangeRateService.findAll().toApiModel() }
}
