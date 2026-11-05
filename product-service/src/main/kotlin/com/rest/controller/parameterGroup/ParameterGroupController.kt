package com.rest.controller.parameterGroup

import com.rest.controller.parameterGroup.adapter.ParameterGroupCreateDtoAdapter
import com.rest.controller.parameterGroup.adapter.ParameterGroupItemAddDtoAdapter
import com.rest.controller.parameterGroup.adapter.ParameterGroupItemResponseAdapter
import com.rest.controller.parameterGroup.adapter.ParameterGroupResponseAdapter
import com.rest.controller.parameterGroup.adapter.ParameterGroupUpdateDtoAdapter
import com.rest.controller.parameterGroup.data.ParameterGroupCreateRequest
import com.rest.controller.parameterGroup.data.ParameterGroupItemAddRequest
import com.rest.controller.parameterGroup.data.ParameterGroupItemResponse
import com.rest.controller.parameterGroup.data.ParameterGroupResponse
import com.rest.controller.parameterGroup.data.ParameterGroupUpdateRequest
import com.service.parameterGroup.ParameterGroupService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/parameter-groups")
class ParameterGroupController(private val parameterGroupService: ParameterGroupService) {

    @GetMapping
    fun getAll(): List<ParameterGroupResponse> =
        with(ParameterGroupResponseAdapter) { parameterGroupService.findAll().toApiModel() }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ParameterGroupResponse =
        with(ParameterGroupResponseAdapter) { parameterGroupService.findById(id).toApiModel() }

    @GetMapping("/{id}/items")
    fun getItems(@PathVariable id: Long): List<ParameterGroupItemResponse> =
        with(ParameterGroupItemResponseAdapter) { parameterGroupService.findItemsByGroupId(id).toApiModel() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody payload: ParameterGroupCreateRequest): ParameterGroupResponse {
        val dto = with(ParameterGroupCreateDtoAdapter) { payload.toServiceModel() }
        return with(ParameterGroupResponseAdapter) { parameterGroupService.create(dto).toApiModel() }
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: ParameterGroupUpdateRequest): ParameterGroupResponse {
        val dto = with(ParameterGroupUpdateDtoAdapter) { request.toServiceModel() }
        return with(ParameterGroupResponseAdapter) { parameterGroupService.update(id, dto).toApiModel() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = parameterGroupService.delete(id)

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@PathVariable id: Long, @RequestBody payload: ParameterGroupItemAddRequest): ParameterGroupItemResponse {
        val dto = with(ParameterGroupItemAddDtoAdapter) { payload.toServiceModel() }
        return with(ParameterGroupItemResponseAdapter) { parameterGroupService.addItem(id, dto).toApiModel() }
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteItem(@PathVariable itemId: Long) = parameterGroupService.deleteItem(itemId)
}