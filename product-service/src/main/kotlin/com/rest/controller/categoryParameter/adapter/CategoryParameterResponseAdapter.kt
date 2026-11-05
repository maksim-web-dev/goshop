package com.rest.controller.categoryParameter.adapter

import com.data.entity.CategoryParameter
import com.rest.controller.categoryParameter.data.CategoryParameterResponse
import com.rest.util.ApiTypeAdapter

object CategoryParameterResponseAdapter : ApiTypeAdapter<CategoryParameterResponse, CategoryParameter>() {

    override fun CategoryParameter.toApiModel() = CategoryParameterResponse(
        id = id,
        name = name,
        order = order,
        categoryId = category.id,
        parameterType = parameterType,
        intValue = intValue,
        doubleValue2 = doubleValue2,
        doubleValue3 = doubleValue3,
        booleanValue = booleanValue,
        stringValue = stringValue,
        parameterGroupId = parameterGroup?.id
    )
}