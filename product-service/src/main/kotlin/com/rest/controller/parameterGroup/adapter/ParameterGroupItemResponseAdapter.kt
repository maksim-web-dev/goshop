package com.rest.controller.parameterGroup.adapter

import com.data.entity.ParameterGroupItem
import com.rest.controller.parameterGroup.data.ParameterGroupItemResponse
import com.rest.util.ApiTypeAdapter

object ParameterGroupItemResponseAdapter : ApiTypeAdapter<ParameterGroupItemResponse, ParameterGroupItem>() {

    override fun ParameterGroupItem.toApiModel() = ParameterGroupItemResponse(
        id = id,
        name = name,
        parameterGroupId = parameterGroup.id
    )
}