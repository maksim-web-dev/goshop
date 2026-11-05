package com.rest.controller.categoryParameter.data

import com.data.entity.ParameterType

data class CategoryParameterResponse(
    val id: Long,
    val name: String,
    val order: Int,
    val categoryId: Long,
    val parameterType: ParameterType,
    val intValue: Int?,
    val doubleValue2: Int?,
    val doubleValue3: Int?,
    val booleanValue: Boolean?,
    val stringValue: String?,
    val parameterGroupId: Long?
)