package com.data.seed

import com.config.props.SeedProps
import com.data.entity.StoreWebhook
import com.data.repository.StoreRepository
import com.data.repository.StoreWebhookRepository
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
class StoreWebhookDataSeed(
    private val storeWebhookRepository: StoreWebhookRepository,
    private val storeRepository: StoreRepository,
    private val seedProps: SeedProps
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!seedProps.enable) return
        if (storeWebhookRepository.count() > seedProps.stopCount) return

        val storesByDomain = storeRepository.findAll().associateBy { it.domain }

        val json = readClassPathResourceAsString("static/data/${seedProps.mode}/store_webhook.json")
        val dtos: Array<WebhookDto> = jacksonObjectMapper().readValue(json)
        storeWebhookRepository.saveAll(dtos.map {
            val store = storesByDomain[it.storeDomain]
                ?: throw IllegalArgumentException("Store '${it.storeDomain}' not found")
            StoreWebhook(
                store = store,
                url = it.url,
                eventTypes = it.eventTypes.toMutableList(),
                secretRef = it.secretRef,
                enabled = it.enabled
            )
        })
    }

    private data class WebhookDto(
        @param:JsonProperty("store_domain")  val storeDomain: String,
        @param:JsonProperty("url")           val url: String,
        @param:JsonProperty("event_types")   val eventTypes: List<String>,
        @param:JsonProperty("secret_ref")    val secretRef: String,
        @param:JsonProperty("enabled")       val enabled: Boolean = true
    )
}
