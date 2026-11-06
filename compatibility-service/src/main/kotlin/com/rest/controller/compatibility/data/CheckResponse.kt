package com.rest.controller.compatibility.data

data class CheckResponse(
    val compatible: Boolean,
    val issues: List<CompatibilityIssueResponse>
)
