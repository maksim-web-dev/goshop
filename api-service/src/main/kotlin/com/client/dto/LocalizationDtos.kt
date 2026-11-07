package com.client.dto

data class BatchResolveRequest(
    val entityType: String,
    val entityIds: List<Long>,
    val fieldNames: List<String>,
    val languageCode: String
)

data class BatchResolveResponse(
    val translations: List<TranslationItem>,
    val missingTranslations: List<MissingTranslationItem>
) {
    data class TranslationItem(
        val entityId: Long,
        val fieldName: String,
        val languageCode: String,
        val value: String,
        val found: Boolean = true
    )
    data class MissingTranslationItem(
        val entityId: Long,
        val fieldName: String,
        val languageCode: String
    )
}
