package com.service.brand.dto

data class CreateBrandDto(
    val name: String,
    val websiteUrl: String? = null
)