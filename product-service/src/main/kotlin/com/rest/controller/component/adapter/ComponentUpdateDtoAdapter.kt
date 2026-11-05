package com.rest.controller.component.adapter

import com.rest.controller.component.data.ComponentUpdateRequest
import com.rest.util.ApiTypeAdapter
import com.service.component.dto.UpdateComponentDto

object ComponentUpdateDtoAdapter : ApiTypeAdapter<ComponentUpdateRequest, UpdateComponentDto>() {

    override fun ComponentUpdateRequest.toServiceModel() = UpdateComponentDto(name = name)
}