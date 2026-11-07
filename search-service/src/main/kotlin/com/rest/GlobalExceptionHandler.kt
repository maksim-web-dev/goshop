package com.rest

import jakarta.validation.ConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(ex.statusCode).body(
            mapOf("status" to ex.statusCode.value(), "error" to (ex.reason ?: ex.message))
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any?>> {
        val details = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(mapOf("status" to 400, "error" to "Validation failed", "details" to details))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<Map<String, Any?>> {
        val details = ex.constraintViolations.map { "${it.propertyPath.last()}: ${it.message}" }
        return ResponseEntity.badRequest().body(mapOf("status" to 400, "error" to "Validation failed", "details" to details))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.internalServerError().body(
            mapOf("status" to 500, "error" to (ex.message ?: "Internal server error"))
        )
}
