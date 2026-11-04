package com.rest.controller.brand.data

data class CreateBrandRequest(
    val name: String,
    val websiteUrl: String? = null
)