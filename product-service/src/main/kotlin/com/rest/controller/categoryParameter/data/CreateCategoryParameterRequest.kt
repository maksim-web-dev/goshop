package com.rest.controller.categoryParameter.data

import com.data.entity.ParameterType

data class CreateCategoryParameterRequest(
    val name: String,
    val order: Int = 0,
    val categoryId: Long,
    val parameterType: ParameterType,
    val intValue: Int? = null,
    val doubleValue2: Int? = null,
    val doubleValue3: Int? = null,
    val booleanValue: Boolean? = null,
    val stringValue: String? = null,
    val parameterGroupId: Long? = null
)