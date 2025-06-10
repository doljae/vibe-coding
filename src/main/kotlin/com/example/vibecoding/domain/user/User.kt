package com.example.vibecoding.domain.user

import java.time.LocalDateTime
import java.util.*

/**
 * User domain entity representing a blog user
 */
data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val displayName: String,
    val bio: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    init {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(username.length >= 3) { "Username must be at least 3 characters" }
        require(username.length <= 50) { "Username cannot exceed 50 characters" }
        require(username.matches(Regex("^[a-zA-Z0-9_]+$"))) { "Username can only contain letters, numbers, and underscores" }
        
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) { "Invalid email format" }
        
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
        require(displayName.length <= 100) { "Display name cannot exceed 100 characters" }
        
        bio?.let { 
            require(it.length <= 500) { "Bio cannot exceed 500 characters" }
        }
    }

    fun updateDisplayName(newDisplayName: String): User {
        require(newDisplayName.isNotBlank()) { "Display name cannot be blank" }
        require(newDisplayName.length <= 100) { "Display name cannot exceed 100 characters" }
        
        return copy(
            displayName = newDisplayName,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateBio(newBio: String?): User {
        newBio?.let { 
            require(it.length <= 500) { "Bio cannot exceed 500 characters" }
        }
        
        return copy(
            bio = newBio,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateEmail(newEmail: String): User {
        require(newEmail.isNotBlank()) { "Email cannot be blank" }
        require(newEmail.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) { "Invalid email format" }
        
        return copy(
            email = newEmail,
            updatedAt = LocalDateTime.now()
        )
    }
}

/**
 * Value object for User ID
 */
@JvmInline
value class UserId(val value: UUID) {
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID())
        fun from(value: String): UserId = UserId(UUID.fromString(value))
    }
}

