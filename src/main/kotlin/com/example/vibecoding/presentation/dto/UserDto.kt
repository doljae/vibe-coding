package com.example.vibecoding.presentation.dto

import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Request DTO for creating a new user
 */
data class CreateUserRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    val username: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    
    @field:NotBlank(message = "Display name is required")
    @field:Size(max = 100, message = "Display name cannot exceed 100 characters")
    val displayName: String,
    
    @field:Size(max = 500, message = "Bio cannot exceed 500 characters")
    val bio: String? = null
)

/**
 * Request DTO for updating an existing user
 */
data class UpdateUserRequest(
    @field:Size(max = 100, message = "Display name cannot exceed 100 characters")
    val displayName: String? = null,
    
    @field:Email(message = "Invalid email format")
    val email: String? = null,
    
    @field:Size(max = 500, message = "Bio cannot exceed 500 characters")
    val bio: String? = null
)

/**
 * Response DTO for user information
 */
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val bio: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id.value.toString(),
                username = user.username,
                email = user.email,
                displayName = user.displayName,
                bio = user.bio,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}

/**
 * Summary response DTO for user lists
 */
data class UserSummaryResponse(
    val id: String,
    val username: String,
    val displayName: String
) {
    companion object {
        fun from(user: User): UserSummaryResponse {
            return UserSummaryResponse(
                id = user.id.value.toString(),
                username = user.username,
                displayName = user.displayName
            )
        }
    }
}

