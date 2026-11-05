package com.rest.controller.component.adapter

import com.rest.controller.component.data.ComponentGroupCreateRequest
import com.rest.util.ApiTypeAdapter
import com.service.component.dto.CreateComponentGroupDto

object ComponentGroupCreateDtoAdapter : ApiTypeAdapter<ComponentGroupCreateRequest, CreateComponentGroupDto>() {

    override fun ComponentGroupCreateRequest.toServiceModel() = CreateComponentGroupDto(name = name)
}