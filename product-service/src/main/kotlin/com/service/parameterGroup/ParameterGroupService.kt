package com.service.parameterGroup

import com.data.entity.ParameterGroup
import com.data.entity.ParameterGroupItem
import com.data.repository.ParameterGroupItemRepository
import com.data.repository.ParameterGroupRepository
import com.service.parameterGroup.dto.AddItemDto
import com.service.parameterGroup.dto.CreateParameterGroupDto
import com.service.parameterGroup.dto.UpdateParameterGroupDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ParameterGroupService(
    private val parameterGroupRepository: ParameterGroupRepository,
    private val parameterGroupItemRepository: ParameterGroupItemRepository
) {

    fun findAll(): List<ParameterGroup> = parameterGroupRepository.findAll()

    fun findById(id: Long): ParameterGroup =
        parameterGroupRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "ParameterGroup $id not found") }

    fun findItemsByGroupId(groupId: Long): List<ParameterGroupItem> {
        findById(groupId)
        return parameterGroupItemRepository.findByParameterGroupId(groupId)
    }

    @Transactional
    fun create(dto: CreateParameterGroupDto): ParameterGroup =
        parameterGroupRepository.save(ParameterGroup(name = dto.name))

    @Transactional
    fun update(id: Long, dto: UpdateParameterGroupDto): ParameterGroup {
        val existing = findById(id)
        return parameterGroupRepository.save(ParameterGroup(id = existing.id, name = dto.name))
    }

    @Transactional
    fun delete(id: Long) {
        if (!parameterGroupRepository.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "ParameterGroup $id not found")
        parameterGroupRepository.deleteById(id)
    }

    @Transactional
    fun addItem(groupId: Long, dto: AddItemDto): ParameterGroupItem {
        val group = findById(groupId)
        return parameterGroupItemRepository.save(ParameterGroupItem(name = dto.name, parameterGroup = group))
    }

    @Transactional
    fun deleteItem(itemId: Long) {
        if (!parameterGroupItemRepository.existsById(itemId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "ParameterGroupItem $itemId not found")
        parameterGroupItemRepository.deleteById(itemId)
    }
}