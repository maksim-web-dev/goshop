package com.rest.controller.brand.adapter

import com.rest.controller.brand.data.BrandCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.brand.dto.CreateBrandDto

object BrandCreateDtoAdapter : ApiTypeAdapter<BrandCreateRequest, CreateBrandDto>() {

    override fun BrandCreateRequest.toServiceModel() = CreateBrandDto(
        name = name,
        websiteUrl = websiteUrl
    )
}