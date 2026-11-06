package com.data.repository

import com.data.entity.Language
import org.springframework.data.jpa.repository.JpaRepository

interface LanguageRepository : JpaRepository<Language, String> {
    fun findByEnabledTrue(): List<Language>
}
