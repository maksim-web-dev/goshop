package com.service.component

import com.data.entity.Component
import com.data.entity.ComponentGroup
import com.data.repository.ComponentGroupRepository
import com.data.repository.ComponentRepository
import com.service.component.dto.CreateComponentDto
import com.service.component.dto.CreateComponentGroupDto
import com.service.component.dto.UpdateComponentDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ComponentService(
    private val componentRepository: ComponentRepository,
    private val componentGroupRepository: ComponentGroupRepository
) {

    fun findAllGroups(): List<ComponentGroup> = componentGroupRepository.findAll()

    fun findByGroupId(groupId: Long): List<Component> {
        if (!componentGroupRepository.existsById(groupId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "ComponentGroup $groupId not found")
        return componentRepository.findByComponentGroupId(groupId)
    }

    fun findById(id: Long): Component =
        componentRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Component $id not found") }

    @Transactional
    fun createGroup(dto: CreateComponentGroupDto): ComponentGroup =
        componentGroupRepository.save(ComponentGroup(name = dto.name))

    @Transactional
    fun create(dto: CreateComponentDto): Component {
        val group = componentGroupRepository.findById(dto.componentGroupId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "ComponentGroup ${dto.componentGroupId} not found") }
        return componentRepository.save(Component(name = dto.name, componentGroup = group))
    }

    @Transactional
    fun update(id: Long, dto: UpdateComponentDto): Component {
        val existing = findById(id)
        return componentRepository.save(
            Component(id = existing.id, name = dto.name, componentGroup = existing.componentGroup)
        )
    }

    @Transactional
    fun delete(id: Long) {
        if (!componentRepository.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Component $id not found")
        componentRepository.deleteById(id)
    }
}