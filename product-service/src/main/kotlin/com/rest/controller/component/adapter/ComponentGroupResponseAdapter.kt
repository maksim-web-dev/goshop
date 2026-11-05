package com.rest.controller.component.adapter

import com.data.entity.ComponentGroup
import com.rest.controller.component.data.ComponentGroupResponse
import com.rest.util.ApiTypeAdapter

object ComponentGroupResponseAdapter : ApiTypeAdapter<ComponentGroupResponse, ComponentGroup>() {

    override fun ComponentGroup.toApiModel() = ComponentGroupResponse(id = id, name = name)
}