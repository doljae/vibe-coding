package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

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
        savedCategory shouldBe category
        foundCategory shouldBe category
    }

    @Test
    fun `should return null when category not found by id`() {
        // Given
        val nonExistentId = CategoryId.generate()

        // When
        val result = repository.findById(nonExistentId)

        // Then
        result.shouldBeNull()
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
        result.size shouldBe 2
        result shouldContain category1
        result shouldContain category2
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
        result1 shouldBe category
        result2 shouldBe category
        result3 shouldBe category
    }

    @Test
    fun `should return null when category not found by name`() {
        // Given
        val category = createTestCategory("Technology", "Tech posts")
        repository.save(category)

        // When
        val result = repository.findByName("NonExistent")

        // Then
        result.shouldBeNull()
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
        deleted shouldBe true
        found.shouldBeNull()
    }

    @Test
    fun `should return false when deleting non-existent category`() {
        // Given
        val nonExistentId = CategoryId.generate()

        // When
        val result = repository.delete(nonExistentId)

        // Then
        result.shouldBe(false)
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
        exists shouldBe true
        notExists shouldBe false
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
        exists1 shouldBe true
        exists2 shouldBe true
        exists3 shouldBe true
        notExists shouldBe false
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
        found shouldBe updatedCategory
        found?.name shouldBe "Updated Technology"
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
