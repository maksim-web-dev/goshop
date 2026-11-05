package com.rest.controller.productType.adapter

import com.rest.controller.productType.data.CreateProductTypeRequest
import com.rest.controller.productType.data.UpdateProductTypeRequest
import com.service.productType.dto.CreateProductTypeDto
import com.service.productType.dto.UpdateProductTypeDto

fun CreateProductTypeRequest.toDto() = CreateProductTypeDto(categoryId = categoryId, name = name)
fun UpdateProductTypeRequest.toDto() = UpdateProductTypeDto(categoryId = categoryId, name = name)