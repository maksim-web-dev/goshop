package com.rest.controller.categoryParameter.adapter

import com.rest.controller.categoryParameter.data.CategoryParameterCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.categoryParameter.dto.CreateCategoryParameterDto

object CategoryParameterCreateDtoAdapter : ApiTypeAdapter<CategoryParameterCreateRequest, CreateCategoryParameterDto>() {

    override fun CategoryParameterCreateRequest.toServiceModel() = CreateCategoryParameterDto(
        name = name,
        order = order,
        categoryId = categoryId,
        parameterType = parameterType,
        intValue = intValue,
        doubleValue2 = doubleValue2,
        doubleValue3 = doubleValue3,
        booleanValue = booleanValue,
        stringValue = stringValue,
        parameterGroupId = parameterGroupId
    )
}