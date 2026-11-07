package com.rest.controller.translation.data

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
