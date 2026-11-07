package com.rest

import com.rest.dto.ErrorResponse
import jakarta.validation.ConstraintViolationException
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(
            errorBody(400, "Bad Request", "Validation failed", details)
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val details = ex.constraintViolations.map { "${it.propertyPath.last()}: ${it.message}" }
        return ResponseEntity.badRequest().body(
            errorBody(400, "Bad Request", "Validation failed", details)
        )
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val status = ex.statusCode.value()
        val reason = HttpStatus.resolve(status)?.reasonPhrase ?: status.toString()
        return ResponseEntity.status(ex.statusCode).body(
            errorBody(status, reason, ex.reason ?: ex.message)
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.internalServerError().body(
            errorBody(500, "Internal Server Error", ex.message ?: "Internal server error")
        )

    private fun errorBody(
        status: Int,
        error: String,
        message: String,
        details: List<String>? = null
    ) = ErrorResponse(
        status = status,
        error = error,
        message = message,
        timestamp = Instant.now(),
        traceId = MDC.get("traceId"),
        details = details
    )
}
