package com.rest.controller.parameterGroup.adapter

import com.rest.controller.parameterGroup.data.ParameterGroupUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.parameterGroup.dto.UpdateParameterGroupDto

object ParameterGroupUpdateDtoAdapter : ApiTypeAdapter<ParameterGroupUpdateRequest, UpdateParameterGroupDto>() {

    override fun ParameterGroupUpdateRequest.toServiceModel() = UpdateParameterGroupDto(name = name)
}