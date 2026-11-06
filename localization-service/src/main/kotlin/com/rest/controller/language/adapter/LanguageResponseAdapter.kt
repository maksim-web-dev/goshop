package com.rest.controller.language.adapter

import com.data.entity.Language
import com.rest.controller.language.data.LanguageResponse
import com.rest.util.ApiTypeAdapter

object LanguageResponseAdapter : ApiTypeAdapter<LanguageResponse, Language>() {
    override fun Language.toApiModel() = LanguageResponse(code = code, name = name, enabled = enabled)
}
