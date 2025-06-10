package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.*

class InMemoryCategoryRepositoryTest {

    private lateinit var repository: InMemoryCategoryRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryCategoryRepository()
    }

    @Test
    fun `should save and find category by id`() {
        // Given
        val category = createTestCategory("Technology", "Tech posts")

        // When
        val savedCategory = repository.save(category)
        val foundCategory = repository.findById(category.id)

        // Then
        assertEquals(savedCategory, category)
        assertEquals(foundCategory, category)
    }

    @Test
    fun `should return null when category not found by id`() {
        // Given
        val nonExistentId = CategoryId.generate()

        // When
        val result = repository.findById(nonExistentId)

        // Then
        assertNull(result)
    }

    @Test
    fun `should find all categories sorted by creation time`() {
        // Given
        val category1 = createTestCategory("Technology", "Tech posts")
        val category2 = createTestCategory("Science", "Science posts")
        
        repository.save(category1)
        repository.save(category2)

        // When
        val result = repository.findAll()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains(category1))
        assertTrue(result.contains(category2))
    }

    @Test
    fun `should find category by name case insensitive`() {
        // Given
        val category = createTestCategory("Technology", "Tech posts")
        repository.save(category)

        // When
        val result1 = repository.findByName("Technology")
        val result2 = repository.findByName("TECHNOLOGY")
        val result3 = repository.findByName("technology")

        // Then
        assertEquals(category, result1)
        assertEquals(category, result2)
        assertEquals(category, result3)
    }

    @Test
    fun `should return null when category not found by name`() {
        // Given
        val category = createTestCategory("Technology", "Tech posts")
        repository.save(category)

        // When
        val result = repository.findByName("NonExistent")

        // Then
        assertNull(result)
    }

    @Test
    fun `should delete category successfully`() {
        // Given
        val category = createTestCategory("Technology", "Tech posts")
        repository.save(category)

        // When
        val deleted = repository.delete(category.id)
        val found = repository.findById(category.id)

        // Then
        assertTrue(deleted)
        assertNull(found)
    }

    @Test
    fun `should return false when deleting non-existent category`() {
        // Given
        val nonExistentId = CategoryId.generate()

        // When
        val result = repository.delete(nonExistentId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should check if category exists by id`() {
        // Given
        val category = createTestCategory("Technology", "Tech posts")
        repository.save(category)

        // When
        val exists = repository.existsById(category.id)
        val notExists = repository.existsById(CategoryId.generate())

        // Then
        assertTrue(exists)
        assertFalse(notExists)
    }

    @Test
    fun `should check if category exists by name case insensitive`() {
        // Given
        val category = createTestCategory("Technology", "Tech posts")
        repository.save(category)

        // When
        val exists1 = repository.existsByName("Technology")
        val exists2 = repository.existsByName("TECHNOLOGY")
        val exists3 = repository.existsByName("technology")
        val notExists = repository.existsByName("NonExistent")

        // Then
        assertTrue(exists1)
        assertTrue(exists2)
        assertTrue(exists3)
        assertFalse(notExists)
    }

    @Test
    fun `should update existing category`() {
        // Given
        val category = createTestCategory("Technology", "Tech posts")
        repository.save(category)
        
        val updatedCategory = category.copy(name = "Updated Technology")

        // When
        repository.save(updatedCategory)
        val found = repository.findById(category.id)

        // Then
        assertEquals(updatedCategory, found)
        assertEquals("Updated Technology", found?.name)
    }

    private fun createTestCategory(name: String, description: String): Category {
        val now = LocalDateTime.now()
        return Category(
            id = CategoryId.generate(),
            name = name,
            description = description,
            createdAt = now,
            updatedAt = now
        )
    }
}

