package com.service.store.dto

data class CreateStoreDto(
    val name: String,
    val domain: String,
    val countryCode: String,
    val defaultLanguage: String,
    val defaultCurrency: String
)
