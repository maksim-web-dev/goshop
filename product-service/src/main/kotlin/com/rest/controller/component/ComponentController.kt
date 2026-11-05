package com.rest.controller.component

import com.rest.controller.component.adapter.ComponentCreateDtoAdapter
import com.rest.controller.component.adapter.ComponentGroupCreateDtoAdapter
import com.rest.controller.component.adapter.ComponentGroupResponseAdapter
import com.rest.controller.component.adapter.ComponentResponseAdapter
import com.rest.controller.component.adapter.ComponentUpdateDtoAdapter
import com.rest.controller.component.data.ComponentCreateRequest
import com.rest.controller.component.data.ComponentGroupCreateRequest
import com.rest.controller.component.data.ComponentGroupResponse
import com.rest.controller.component.data.ComponentResponse
import com.rest.controller.component.data.ComponentUpdateRequest
import com.service.component.ComponentService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/components")
class ComponentController(private val componentService: ComponentService) {

    @GetMapping("/groups")
    fun getAllGroups(): List<ComponentGroupResponse> =
        with(ComponentGroupResponseAdapter) { componentService.findAllGroups().toApiModel() }

    @GetMapping
    fun getByGroup(@RequestParam groupId: Long): List<ComponentResponse> =
        with(ComponentResponseAdapter) { componentService.findByGroupId(groupId).toApiModel() }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ComponentResponse =
        with(ComponentResponseAdapter) { componentService.findById(id).toApiModel() }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    fun createGroup(@RequestBody payload: ComponentGroupCreateRequest): ComponentGroupResponse {
        val dto = with(ComponentGroupCreateDtoAdapter) { payload.toServiceModel() }
        return with(ComponentGroupResponseAdapter) { componentService.createGroup(dto).toApiModel() }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody payload: ComponentCreateRequest): ComponentResponse {
        val dto = with(ComponentCreateDtoAdapter) { payload.toServiceModel() }
        return with(ComponentResponseAdapter) { componentService.create(dto).toApiModel() }
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: ComponentUpdateRequest): ComponentResponse {
        val dto = with(ComponentUpdateDtoAdapter) { request.toServiceModel() }
        return with(ComponentResponseAdapter) { componentService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = componentService.delete(id)
}