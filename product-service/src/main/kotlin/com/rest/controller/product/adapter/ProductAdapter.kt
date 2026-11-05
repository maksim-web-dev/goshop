package com.rest.controller.product.adapter

import com.rest.controller.product.data.CreateProductRequest
import com.rest.controller.product.data.UpdateProductRequest
import com.service.product.dto.CreateProductDto
import com.service.product.dto.UpdateProductDto

fun CreateProductRequest.toDto() = CreateProductDto(
    sku = sku,
    name = name,
    brandId = brandId,
    categoryId = categoryId,
    productTypeId = productTypeId,
    description = description
)

fun UpdateProductRequest.toDto() = UpdateProductDto(
    name = name,
    brandId = brandId,
    categoryId = categoryId,
    productTypeId = productTypeId,
    description = description,
    status = status
)