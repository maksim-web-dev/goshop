package com.rest.controller.product.adapter

import com.rest.controller.product.data.ProductDetailsResponse
import com.service.product.dto.ProductWithAttributesDto

object ProductDetailsResponseAdapter {

    fun ProductWithAttributesDto.toApiModel() = ProductDetailsResponse(
        id = product.id,
        sku = product.sku,
        name = product.name,
        description = product.description,
        status = product.status,
        brand = ProductDetailsResponse.BrandInfo(
            id = product.brand.id,
            name = product.brand.name,
            websiteUrl = product.brand.websiteUrl
        ),
        category = ProductDetailsResponse.CategoryInfo(
            id = product.category.id,
            name = product.category.name,
            slug = product.category.slug
        ),
        productType = ProductDetailsResponse.ProductTypeInfo(
            id = product.productType.id,
            name = product.productType.name
        ),
        attributes = attributes.map { pa ->
            ProductDetailsResponse.AttributeInfo(
                code = pa.categoryParameter.code,
                name = pa.categoryParameter.name,
                value = pa.value
            )
        },
        createdAt = product.createdAt,
        updatedAt = product.updatedAt
    )
}
