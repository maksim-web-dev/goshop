package com.rest.controller.parameterGroup.adapter

import com.data.entity.ParameterGroup
import com.rest.controller.parameterGroup.data.ParameterGroupResponse
import com.rest.util.ApiTypeAdapter

object ParameterGroupResponseAdapter : ApiTypeAdapter<ParameterGroupResponse, ParameterGroup>() {

    override fun ParameterGroup.toApiModel() = ParameterGroupResponse(id = id, name = name)
}