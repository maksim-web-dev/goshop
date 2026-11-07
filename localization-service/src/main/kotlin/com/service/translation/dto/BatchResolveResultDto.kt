package com.service.translation.dto

data class BatchResolveResultDto(
    val found: List<FoundItem>,
    val missing: List<MissingItem>
) {
    data class FoundItem(val entityId: Long, val fieldName: String, val value: String)
    data class MissingItem(val entityId: Long, val fieldName: String)
}
