package com.rest.controller.parameterGroup

import com.data.entity.ParameterGroup
import com.data.entity.ParameterGroupItem
import com.rest.controller.parameterGroup.adapter.toDto
import com.rest.controller.parameterGroup.data.AddItemRequest
import com.rest.controller.parameterGroup.data.CreateParameterGroupRequest
import com.rest.controller.parameterGroup.data.UpdateParameterGroupRequest
import com.service.parameterGroup.ParameterGroupService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/parameter-groups")
class ParameterGroupController(private val parameterGroupService: ParameterGroupService) {

    @GetMapping
    fun getAll(): List<ParameterGroup> = parameterGroupService.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ParameterGroup = parameterGroupService.findById(id)

    @GetMapping("/{id}/items")
    fun getItems(@PathVariable id: Long): List<ParameterGroupItem> =
        parameterGroupService.findItemsByGroupId(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateParameterGroupRequest): ParameterGroup =
        parameterGroupService.create(request.toDto())

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: UpdateParameterGroupRequest): ParameterGroup =
        parameterGroupService.update(id, request.toDto())

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = parameterGroupService.delete(id)

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@PathVariable id: Long, @RequestBody request: AddItemRequest): ParameterGroupItem =
        parameterGroupService.addItem(id, request.toDto())

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteItem(@PathVariable itemId: Long) = parameterGroupService.deleteItem(itemId)
}