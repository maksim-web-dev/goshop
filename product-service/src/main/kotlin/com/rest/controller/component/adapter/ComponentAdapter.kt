package com.rest.controller.component.adapter

import com.rest.controller.component.data.CreateComponentGroupRequest
import com.rest.controller.component.data.CreateComponentRequest
import com.rest.controller.component.data.UpdateComponentRequest
import com.service.component.dto.CreateComponentDto
import com.service.component.dto.CreateComponentGroupDto
import com.service.component.dto.UpdateComponentDto

fun CreateComponentGroupRequest.toDto() = CreateComponentGroupDto(name = name)
fun CreateComponentRequest.toDto() = CreateComponentDto(name = name, componentGroupId = componentGroupId)
fun UpdateComponentRequest.toDto() = UpdateComponentDto(name = name)