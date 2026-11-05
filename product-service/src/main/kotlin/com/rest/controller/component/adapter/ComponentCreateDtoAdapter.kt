package com.rest.controller.component.adapter

import com.rest.controller.component.data.ComponentCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.component.dto.CreateComponentDto

object ComponentCreateDtoAdapter : ApiTypeAdapter<ComponentCreateRequest, CreateComponentDto>() {

    override fun ComponentCreateRequest.toServiceModel() = CreateComponentDto(
        name = name,
        componentGroupId = componentGroupId
    )
}