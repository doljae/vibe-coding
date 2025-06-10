package com.example.vibecoding.domain.category

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CategoryTest {

    @Test
    fun `should create category with valid data`() {
        // Given
        val id = CategoryId.generate()
        val name = "Technology"
        val description = "Tech related posts"
        val now = LocalDateTime.now()

        // When
        val category = Category(
            id = id,
            name = name,
            description = description,
            createdAt = now,
            updatedAt = now
        )

        // Then
        category.id shouldBe id
        category.name shouldBe name
        category.description shouldBe description
        category.createdAt shouldBe now
        category.updatedAt shouldBe now
    }

    @Test
    fun `should throw exception when name is blank`() {
        // Given
        val id = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Category(
                id = id,
                name = "",
                description = "Description",
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when name exceeds 100 characters`() {
        // Given
        val id = CategoryId.generate()
        val longName = "a".repeat(101)
        val now = LocalDateTime.now()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Category(
                id = id,
                name = longName,
                description = "Description",
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when description exceeds 500 characters`() {
        // Given
        val id = CategoryId.generate()
        val longDescription = "a".repeat(501)
        val now = LocalDateTime.now()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Category(
                id = id,
                name = "Technology",
                description = longDescription,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should update name successfully`() {
        // Given
        val category = createTestCategory("Technology", "Description")
        val newName = "Updated Technology"

        // When
        val updatedCategory = category.updateName(newName)

        // Then
        updatedCategory.name shouldBe newName
        updatedCategory.description shouldBe category.description
        updatedCategory.id shouldBe category.id
        updatedCategory.createdAt shouldBe category.createdAt
        updatedCategory.updatedAt shouldNotBe category.updatedAt
    }

    @Test
    fun `should update description successfully`() {
        // Given
        val category = createTestCategory("Technology", "Old description")
        val newDescription = "Updated description"

        // When
        val updatedCategory = category.updateDescription(newDescription)

        // Then
        updatedCategory.description shouldBe newDescription
        updatedCategory.name shouldBe category.name
        updatedCategory.id shouldBe category.id
        updatedCategory.createdAt shouldBe category.createdAt
        updatedCategory.updatedAt shouldNotBe category.updatedAt
    }

    @Test
    fun `should generate unique category IDs`() {
        // When
        val id1 = CategoryId.generate()
        val id2 = CategoryId.generate()

        // Then
        id1 shouldNotBe id2
    }

    @Test
    fun `should create category ID from string`() {
        // Given
        val uuidString = "123e4567-e89b-12d3-a456-426614174000"

        // When
        val categoryId = CategoryId.from(uuidString)

        // Then
        categoryId.value.toString() shouldBe uuidString
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
