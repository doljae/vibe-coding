package com.example.vibecoding.presentation.dto

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Request DTO for creating a new category
 */
data class CreateCategoryRequest(
    @field:NotBlank(message = "Category name is required")
    @field:Size(max = 100, message = "Category name cannot exceed 100 characters")
    val name: String,
    
    @field:Size(max = 500, message = "Category description cannot exceed 500 characters")
    val description: String? = null
)

/**
 * Request DTO for updating an existing category
 */
data class UpdateCategoryRequest(
    @field:Size(max = 100, message = "Category name cannot exceed 100 characters")
    val name: String? = null,
    
    @field:Size(max = 500, message = "Category description cannot exceed 500 characters")
    val description: String? = null
)

/**
 * Response DTO for category information
 */
data class CategoryResponse(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(category: Category): CategoryResponse {
            return CategoryResponse(
                id = category.id.value.toString(),
                name = category.name,
                description = category.description,
                createdAt = category.createdAt,
                updatedAt = category.updatedAt
            )
        }
    }
}

/**
 * Summary response DTO for category lists
 */
data class CategorySummaryResponse(
    val id: String,
    val name: String,
    val description: String?
) {
    companion object {
        fun from(category: Category): CategorySummaryResponse {
            return CategorySummaryResponse(
                id = category.id.value.toString(),
                name = category.name,
                description = category.description
            )
        }
    }
}

