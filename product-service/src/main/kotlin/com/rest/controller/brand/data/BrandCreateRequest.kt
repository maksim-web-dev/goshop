package com.rest.controller.brand.data

data class BrandCreateRequest(
    val name: String,
    val websiteUrl: String? = null
)