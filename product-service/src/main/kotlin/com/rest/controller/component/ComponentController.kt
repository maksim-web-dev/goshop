package com.rest.controller.component

import com.data.entity.Component
import com.data.entity.ComponentGroup
import com.rest.controller.component.adapter.toDto
import com.rest.controller.component.data.CreateComponentGroupRequest
import com.rest.controller.component.data.CreateComponentRequest
import com.rest.controller.component.data.UpdateComponentRequest
import com.service.component.ComponentService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/components")
class ComponentController(private val componentService: ComponentService) {

    @GetMapping("/groups")
    fun getAllGroups(): List<ComponentGroup> = componentService.findAllGroups()

    @GetMapping
    fun getByGroup(@RequestParam groupId: Long): List<Component> =
        componentService.findByGroupId(groupId)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Component = componentService.findById(id)

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    fun createGroup(@RequestBody request: CreateComponentGroupRequest): ComponentGroup =
        componentService.createGroup(request.toDto())

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateComponentRequest): Component =
        componentService.create(request.toDto())

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: UpdateComponentRequest): Component =
        componentService.update(id, request.toDto())

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = componentService.delete(id)
}