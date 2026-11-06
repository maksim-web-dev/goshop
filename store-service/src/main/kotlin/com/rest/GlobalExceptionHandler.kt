package com.rest

import org.springframework.http.ResponseEntity
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

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.internalServerError().body(
            mapOf("status" to 500, "error" to (ex.message ?: "Internal server error"))
        )
}
