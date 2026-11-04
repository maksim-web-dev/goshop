package com.rest.controller.brand.adapter

import com.rest.controller.brand.data.CreateBrandRequest
import com.rest.controller.brand.data.UpdateBrandRequest
import com.service.brand.dto.CreateBrandDto
import com.service.brand.dto.UpdateBrandDto

fun CreateBrandRequest.toDto() = CreateBrandDto(name = name, websiteUrl = websiteUrl)
fun UpdateBrandRequest.toDto() = UpdateBrandDto(name = name, websiteUrl = websiteUrl)