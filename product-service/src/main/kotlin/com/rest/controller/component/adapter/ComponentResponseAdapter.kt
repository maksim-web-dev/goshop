package com.rest.controller.component.adapter

import com.data.entity.Component
import com.rest.controller.component.data.ComponentResponse
import com.rest.util.ApiTypeAdapter

object ComponentResponseAdapter : ApiTypeAdapter<ComponentResponse, Component>() {

    override fun Component.toApiModel() = ComponentResponse(
        id = id,
        name = name,
        componentGroupId = componentGroup.id
    )
}