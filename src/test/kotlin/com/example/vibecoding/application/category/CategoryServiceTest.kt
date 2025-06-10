package com.example.vibecoding.application.category

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.PostRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDateTime
import kotlin.test.assertEquals

class CategoryServiceTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var postRepository: PostRepository
    private lateinit var categoryService: CategoryService

    @BeforeEach
    fun setUp() {
        categoryRepository = mock()
        postRepository = mock()
        categoryService = CategoryService(categoryRepository, postRepository)
    }

    @Test
    fun `should create category successfully`() {
        // Given
        val name = "Technology"
        val description = "Tech related posts"
        
        whenever(categoryRepository.existsByName(name)).thenReturn(false)
        whenever(categoryRepository.save(any())).thenAnswer { it.arguments[0] as Category }

        // When
        val result = categoryService.createCategory(name, description)

        // Then
        assertEquals(name, result.name)
        assertEquals(description, result.description)
    }

    @Test
    fun `should throw exception when creating category with existing name`() {
        // Given
        val name = "Technology"
        val description = "Tech related posts"
        
        whenever(categoryRepository.existsByName(name)).thenReturn(true)

        // When & Then
        assertThrows<CategoryAlreadyExistsException> {
            categoryService.createCategory(name, description)
        }
    }

    @Test
    fun `should update category successfully`() {
        // Given
        val categoryId = CategoryId.generate()
        val existingCategory = createTestCategory(categoryId, "Technology", "Old description")
        val newName = "Updated Technology"
        val newDescription = "Updated description"
        
        whenever(categoryRepository.findById(categoryId)).thenReturn(existingCategory)
        whenever(categoryRepository.existsByName(newName)).thenReturn(false)
        whenever(categoryRepository.save(any())).thenAnswer { it.arguments[0] as Category }

        // When
        val result = categoryService.updateCategory(categoryId, newName, newDescription)

        // Then
        assertEquals(newName, result.name)
        assertEquals(newDescription, result.description)
    }

    @Test
    fun `should throw exception when updating non-existent category`() {
        // Given
        val categoryId = CategoryId.generate()
        
        whenever(categoryRepository.findById(categoryId)).thenReturn(null)

        // When & Then
        assertThrows<CategoryNotFoundException> {
            categoryService.updateCategory(categoryId, "New Name", "New Description")
        }
    }

    @Test
    fun `should get category by id successfully`() {
        // Given
        val categoryId = CategoryId.generate()
        val category = createTestCategory(categoryId, "Technology", "Description")
        
        whenever(categoryRepository.findById(categoryId)).thenReturn(category)

        // When
        val result = categoryService.getCategoryById(categoryId)

        // Then
        assertEquals(category, result)
    }

    @Test
    fun `should throw exception when getting non-existent category`() {
        // Given
        val categoryId = CategoryId.generate()
        
        whenever(categoryRepository.findById(categoryId)).thenReturn(null)

        // When & Then
        assertThrows<CategoryNotFoundException> {
            categoryService.getCategoryById(categoryId)
        }
    }

    private fun createTestCategory(id: CategoryId, name: String, description: String): Category {
        val now = LocalDateTime.now()
        return Category(
            id = id,
            name = name,
            description = description,
            createdAt = now,
            updatedAt = now
        )
    }
}

