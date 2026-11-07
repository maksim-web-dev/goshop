package com.rest.controller.internal.data

import jakarta.validation.constraints.NotBlank

data class ApiKeyValidateRequest(
    @field:NotBlank
    val apiKey: String
)
