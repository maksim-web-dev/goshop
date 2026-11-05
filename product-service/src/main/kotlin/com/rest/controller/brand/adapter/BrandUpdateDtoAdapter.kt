package com.rest.controller.brand.adapter

import com.rest.controller.brand.data.BrandUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.brand.dto.UpdateBrandDto

object BrandUpdateDtoAdapter : ApiTypeAdapter<BrandUpdateRequest, UpdateBrandDto>() {

    override fun BrandUpdateRequest.toServiceModel() = UpdateBrandDto(
        name = name,
        websiteUrl = websiteUrl
    )
}