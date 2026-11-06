package com.rest.controller.store.data

data class StoreCreateRequest(
    val name: String,
    val domain: String,
    val countryCode: String,
    val defaultLanguage: String,
    val defaultCurrency: String
)
