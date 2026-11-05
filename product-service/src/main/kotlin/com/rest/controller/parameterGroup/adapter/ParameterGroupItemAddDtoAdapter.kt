package com.rest.controller.parameterGroup.adapter

import com.rest.controller.parameterGroup.data.ParameterGroupItemAddRequest
import com.rest.util.ApiTypeAdapter
import com.service.parameterGroup.dto.AddItemDto

object ParameterGroupItemAddDtoAdapter : ApiTypeAdapter<ParameterGroupItemAddRequest, AddItemDto>() {

    override fun ParameterGroupItemAddRequest.toServiceModel() = AddItemDto(name = name)
}