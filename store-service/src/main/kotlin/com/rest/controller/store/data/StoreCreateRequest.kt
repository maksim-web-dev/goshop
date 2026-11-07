package com.rest.controller.store.data

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class StoreCreateRequest(
    @field:NotBlank @field:Size(max = 255)
    val name: String,

    @field:NotBlank @field:Size(max = 255)
    val domain: String,

    @field:NotBlank @field:Size(min = 2, max = 2)
    val countryCode: String,

    @field:NotBlank @field:Size(min = 2, max = 10)
    val defaultLanguage: String,

    @field:NotBlank @field:Size(min = 3, max = 3)
    val defaultCurrency: String
)
