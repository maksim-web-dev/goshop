package com.rest.controller.parameterGroup.adapter

import com.rest.controller.parameterGroup.data.AddItemRequest
import com.rest.controller.parameterGroup.data.CreateParameterGroupRequest
import com.rest.controller.parameterGroup.data.UpdateParameterGroupRequest
import com.service.parameterGroup.dto.AddItemDto
import com.service.parameterGroup.dto.CreateParameterGroupDto
import com.service.parameterGroup.dto.UpdateParameterGroupDto

fun CreateParameterGroupRequest.toDto() = CreateParameterGroupDto(name = name)
fun UpdateParameterGroupRequest.toDto() = UpdateParameterGroupDto(name = name)
fun AddItemRequest.toDto() = AddItemDto(name = name)