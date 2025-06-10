package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.category.CategoryService
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.presentation.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for category management operations
 */
@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    /**
     * Get all categories
     */
    @GetMapping
    fun getAllCategories(): ResponseEntity<List<CategorySummaryResponse>> {
        val categories = categoryService.getAllCategories()
        val response = categories.map { CategorySummaryResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: String): ResponseEntity<CategoryResponse> {
        val categoryId = CategoryId.from(id)
        val category = categoryService.getCategoryById(categoryId)
        val response = CategoryResponse.from(category)
        return ResponseEntity.ok(response)
    }

    /**
     * Create a new category
     */
    @PostMapping
    fun createCategory(@Valid @RequestBody request: CreateCategoryRequest): ResponseEntity<CategoryResponse> {
        val category = categoryService.createCategory(
            name = request.name,
            description = request.description
        )
        val response = CategoryResponse.from(category)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Update an existing category
     */
    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateCategoryRequest
    ): ResponseEntity<CategoryResponse> {
        val categoryId = CategoryId.from(id)
        val category = categoryService.updateCategory(
            id = categoryId,
            name = request.name,
            description = request.description
        )
        val response = CategoryResponse.from(category)
        return ResponseEntity.ok(response)
    }

    /**
     * Delete a category
     */
    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: String): ResponseEntity<Void> {
        val categoryId = CategoryId.from(id)
        categoryService.deleteCategory(categoryId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Get category by name
     */
    @GetMapping("/search")
    fun getCategoryByName(@RequestParam name: String): ResponseEntity<CategoryResponse> {
        val category = categoryService.getCategoryByName(name)
            ?: return ResponseEntity.notFound().build()
        val response = CategoryResponse.from(category)
        return ResponseEntity.ok(response)
    }
}

