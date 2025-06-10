package com.example.vibecoding.domain.category

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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
        assertEquals(id, category.id)
        assertEquals(name, category.name)
        assertEquals(description, category.description)
        assertEquals(now, category.createdAt)
        assertEquals(now, category.updatedAt)
    }

    @Test
    fun `should throw exception when name is blank`() {
        // Given
        val id = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        assertThrows<IllegalArgumentException> {
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
        assertThrows<IllegalArgumentException> {
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
        assertThrows<IllegalArgumentException> {
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
    fun `should allow null description`() {
        // Given
        val id = CategoryId.generate()
        val now = LocalDateTime.now()

        // When
        val category = Category(
            id = id,
            name = "Technology",
            description = null,
            createdAt = now,
            updatedAt = now
        )

        // Then
        assertEquals(null, category.description)
    }

    @Test
    fun `should update name successfully`() {
        // Given
        val category = createTestCategory()
        val newName = "Updated Technology"

        // When
        val updatedCategory = category.updateName(newName)

        // Then
        assertEquals(newName, updatedCategory.name)
        assertEquals(category.id, updatedCategory.id)
        assertEquals(category.description, updatedCategory.description)
        assertEquals(category.createdAt, updatedCategory.createdAt)
        assertNotEquals(category.updatedAt, updatedCategory.updatedAt)
    }

    @Test
    fun `should update description successfully`() {
        // Given
        val category = createTestCategory()
        val newDescription = "Updated description"

        // When
        val updatedCategory = category.updateDescription(newDescription)

        // Then
        assertEquals(newDescription, updatedCategory.description)
        assertEquals(category.id, updatedCategory.id)
        assertEquals(category.name, updatedCategory.name)
        assertEquals(category.createdAt, updatedCategory.createdAt)
        assertNotEquals(category.updatedAt, updatedCategory.updatedAt)
    }

    @Test
    fun `should throw exception when updating with blank name`() {
        // Given
        val category = createTestCategory()

        // When & Then
        assertThrows<IllegalArgumentException> {
            category.updateName("")
        }
    }

    @Test
    fun `should throw exception when updating with long description`() {
        // Given
        val category = createTestCategory()
        val longDescription = "a".repeat(501)

        // When & Then
        assertThrows<IllegalArgumentException> {
            category.updateDescription(longDescription)
        }
    }

    private fun createTestCategory(): Category {
        val now = LocalDateTime.now()
        return Category(
            id = CategoryId.generate(),
            name = "Technology",
            description = "Tech related posts",
            createdAt = now,
            updatedAt = now
        )
    }
}

