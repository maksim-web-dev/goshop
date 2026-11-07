package com.filter

import com.client.StoreClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.ratelimit.RateLimiter
import com.rest.dto.ErrorResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant

@Component
@Order(10)
class StoreAuthFilter(
    private val storeClient: StoreClient,
    private val rateLimiter: RateLimiter,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(StoreAuthFilter::class.java)

    private val publicPaths = setOf(
        "/actuator", "/v3/api-docs", "/swagger-ui", "/swagger-ui.html"
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        publicPaths.any { request.requestURI.startsWith(it) }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader(API_KEY_HEADER)
        if (apiKey.isNullOrBlank()) {
            reject(response, HttpStatus.UNAUTHORIZED, "Missing $API_KEY_HEADER header")
            return
        }

        val validation = runCatching { storeClient.validateApiKey(apiKey) }.getOrElse {
            log.error("Store service unreachable during API key validation", it)
            reject(response, HttpStatus.SERVICE_UNAVAILABLE, "Authentication service unavailable")
            return
        }

        if (!validation.valid) {
            reject(response, HttpStatus.UNAUTHORIZED, "Invalid or expired API key")
            return
        }

        val storeStatus = validation.storeStatus ?: "UNKNOWN"
        if (storeStatus != "ACTIVE") {
            reject(response, HttpStatus.FORBIDDEN, "Store is not active: $storeStatus")
            return
        }

        val storeContext = StoreContext(
            storeId = validation.storeId!!,
            storeStatus = storeStatus,
            defaultLanguage = validation.defaultLanguage ?: "en",
            defaultCurrency = validation.defaultCurrency ?: "USD",
            requestsPerSecond = validation.requestsPerSecond ?: 100,
            burstLimit = validation.burstLimit ?: 200
        )

        if (!rateLimiter.tryAcquire(storeContext.storeId, storeContext.requestsPerSecond, storeContext.burstLimit)) {
            reject(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded")
            return
        }

        request.setAttribute(STORE_CONTEXT_ATTR, storeContext)
        filterChain.doFilter(request, response)
    }

    private fun reject(response: HttpServletResponse, status: HttpStatus, message: String) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val body = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = message,
            timestamp = Instant.now(),
            traceId = null
        )
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}
