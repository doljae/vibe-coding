package com.example.vibecoding.domain.category

import java.time.LocalDateTime
import java.util.*

/**
 * Category domain entity representing a blog category
 */
data class Category(
    val id: CategoryId,
    val name: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    init {
        require(name.isNotBlank()) { "Category name cannot be blank" }
        require(name.length <= 100) { "Category name cannot exceed 100 characters" }
        description?.let { 
            require(it.length <= 500) { "Category description cannot exceed 500 characters" }
        }
    }

    fun updateName(newName: String): Category {
        require(newName.isNotBlank()) { "Category name cannot be blank" }
        require(newName.length <= 100) { "Category name cannot exceed 100 characters" }
        
        return copy(
            name = newName,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateDescription(newDescription: String?): Category {
        newDescription?.let { 
            require(it.length <= 500) { "Category description cannot exceed 500 characters" }
        }
        
        return copy(
            description = newDescription,
            updatedAt = LocalDateTime.now()
        )
    }
}

/**
 * Value object for Category ID
 */
@JvmInline
value class CategoryId(val value: UUID) {
    companion object {
        fun generate(): CategoryId = CategoryId(UUID.randomUUID())
        fun from(value: String): CategoryId = CategoryId(UUID.fromString(value))
    }
}

