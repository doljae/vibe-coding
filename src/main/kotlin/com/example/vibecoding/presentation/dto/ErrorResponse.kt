package com.example.vibecoding.presentation.dto

import java.time.LocalDateTime

/**
 * Standard error response format for API endpoints
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

/**
 * Validation error response with field-specific errors
 */
data class ValidationErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val fieldErrors: List<FieldError>
)

data class FieldError(
    val field: String,
    val rejectedValue: Any?,
    val message: String
)

