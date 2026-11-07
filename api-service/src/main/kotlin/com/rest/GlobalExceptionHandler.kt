package com.rest

import com.rest.dto.ErrorResponse
import jakarta.validation.ConstraintViolationException
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(errorBody(400, "Bad Request", "Validation failed", details))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val details = ex.constraintViolations.map { "${it.propertyPath.last()}: ${it.message}" }
        return ResponseEntity.badRequest().body(errorBody(400, "Bad Request", "Validation failed", details))
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val status = ex.statusCode.value()
        val reason = HttpStatus.resolve(status)?.reasonPhrase ?: status.toString()
        return ResponseEntity.status(ex.statusCode).body(errorBody(status, reason, ex.reason ?: ex.message ?: "Error"))
    }

    @ExceptionHandler(HttpClientErrorException.NotFound::class)
    fun handleNotFound(ex: HttpClientErrorException.NotFound): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(errorBody(404, "Not Found", "Downstream resource not found"))

    @ExceptionHandler(ResourceAccessException::class)
    fun handleDownstreamTimeout(ex: ResourceAccessException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
            .body(errorBody(504, "Gateway Timeout", "Downstream service did not respond in time"))

    @ExceptionHandler(HttpServerErrorException::class)
    fun handleDownstreamError(ex: HttpServerErrorException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(errorBody(502, "Bad Gateway", "Downstream service returned an error"))

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.internalServerError().body(
            errorBody(500, "Internal Server Error", ex.message ?: "Internal server error")
        )

    private fun errorBody(status: Int, error: String, message: String, details: List<String>? = null) =
        ErrorResponse(
            status = status,
            error = error,
            message = message,
            timestamp = Instant.now(),
            traceId = MDC.get("traceId"),
            details = details
        )
}
