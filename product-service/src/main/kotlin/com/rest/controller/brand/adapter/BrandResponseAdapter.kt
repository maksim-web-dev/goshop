package com.rest.controller.brand.adapter

import com.data.entity.Brand
import com.rest.controller.brand.data.BrandResponse
import com.rest.util.ApiTypeAdapter


object BrandResponseAdapter : ApiTypeAdapter<BrandResponse, Brand>() {

    override fun Brand.toApiModel() = BrandResponse(id = id, name = name, websiteUrl = websiteUrl)
}