package com.rest.controller.translation.data

import com.data.entity.EntityType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class BatchResolveRequest(
    val entityType: EntityType,
    @field:NotEmpty(message = "entityIds must not be empty")
    @field:Size(max = 100, message = "entityIds must not exceed 100 items")
    val entityIds: List<Long> = emptyList(),
    @field:NotEmpty(message = "fieldNames must not be empty")
    @field:Size(max = 20, message = "fieldNames must not exceed 20 items")
    val fieldNames: List<String> = emptyList(),
    @field:NotBlank(message = "languageCode must not be blank")
    @field:Size(min = 2, max = 10, message = "languageCode must be 2–10 characters")
    val languageCode: String = ""
)
