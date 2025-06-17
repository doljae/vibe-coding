package com.example.vibecoding.presentation.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

/**
 * Request DTO for liking a comment
 */
data class CommentLikeRequest(
    @field:NotBlank(message = "User ID cannot be blank")
    val userId: String
)

/**
 * Response DTO for a comment like
 */
data class CommentLikeResponse(
    val id: String,
    val commentId: String,
    val userId: String,
    val createdAt: LocalDateTime
)

