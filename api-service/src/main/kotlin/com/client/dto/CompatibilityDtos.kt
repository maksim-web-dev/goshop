package com.client.dto

data class CompatibilityCheckRequest(val productIds: List<Long>)

data class CompatibilityCheckResponse(
    val compatible: Boolean,
    val issues: List<IssueResponse>
) {
    data class IssueResponse(
        val ruleId: Long? = null,
        val severity: String,
        val messageCode: String
    )
}
