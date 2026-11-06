package com.data.seed

import com.config.props.SeedProps
import com.data.entity.Brand
import com.data.repository.BrandRepository
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
@Order(3)
class BrandDataSeed(
    private val brandRepository: BrandRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (brandRepository.count() > seedProps.stopCount) return

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/product_brand.json")
        val dtos: Array<BrandDto> = jacksonObjectMapper().readValue(json)
        brandRepository.saveAll(dtos.map { Brand(name = it.name, websiteUrl = it.websiteUrl) })
    }

    private data class BrandDto(
        @param:JsonProperty("name")        val name: String,
        @param:JsonProperty("website_url") val websiteUrl: String? = null
    )
}