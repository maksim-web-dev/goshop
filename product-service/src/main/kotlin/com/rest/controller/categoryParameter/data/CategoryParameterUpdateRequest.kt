package com.rest.controller.categoryParameter.data

import com.data.entity.ParameterType

data class CategoryParameterUpdateRequest(
    val name: String? = null,
    val order: Int? = null,
    val parameterType: ParameterType? = null,
    val intValue: Int? = null,
    val doubleValue2: Int? = null,
    val doubleValue3: Int? = null,
    val booleanValue: Boolean? = null,
    val stringValue: String? = null,
    val parameterGroupId: Long? = null
)