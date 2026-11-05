package com.rest.controller.parameterGroup.adapter

import com.rest.controller.parameterGroup.data.ParameterGroupCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.parameterGroup.dto.CreateParameterGroupDto

object ParameterGroupCreateDtoAdapter : ApiTypeAdapter<ParameterGroupCreateRequest, CreateParameterGroupDto>() {

    override fun ParameterGroupCreateRequest.toServiceModel() = CreateParameterGroupDto(name = name)
}