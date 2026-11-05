package com.rest.controller.categoryParameter.adapter

import com.rest.controller.categoryParameter.data.CategoryParameterUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.categoryParameter.dto.UpdateCategoryParameterDto

object CategoryParameterUpdateDtoAdapter : ApiTypeAdapter<CategoryParameterUpdateRequest, UpdateCategoryParameterDto>() {

    override fun CategoryParameterUpdateRequest.toServiceModel() = UpdateCategoryParameterDto(
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
}