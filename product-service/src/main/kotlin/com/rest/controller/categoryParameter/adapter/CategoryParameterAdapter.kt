package com.rest.controller.categoryParameter.adapter

import com.rest.controller.categoryParameter.data.CreateCategoryParameterRequest
import com.rest.controller.categoryParameter.data.UpdateCategoryParameterRequest
import com.service.categoryParameter.dto.CreateCategoryParameterDto
import com.service.categoryParameter.dto.UpdateCategoryParameterDto

fun CreateCategoryParameterRequest.toDto() = CreateCategoryParameterDto(
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

fun UpdateCategoryParameterRequest.toDto() = UpdateCategoryParameterDto(
    name = name,
    order = order,
    parameterType = parameterType,
    intValue = intValue,
    doubleValue2 = doubleValue2,
    doubleValue3 = doubleValue3,
    booleanValue = booleanValue,
    stringValue = stringValue,
    parameterGroupId = parameterGroupId
)