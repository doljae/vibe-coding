package com.example.vibecoding.domain.post

import com.example.vibecoding.domain.category.CategoryId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PostTest {

    @Test
    fun `should create post with valid data`() {
        // Given
        val id = PostId.generate()
        val title = "My First Post"
        val content = "This is the content of my first post"
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When
        val post = Post(
            id = id,
            title = title,
            content = content,
            categoryId = categoryId,
            createdAt = now,
            updatedAt = now
        )

        // Then
        assertEquals(id, post.id)
        assertEquals(title, post.title)
        assertEquals(content, post.content)
        assertEquals(categoryId, post.categoryId)
        assertEquals(now, post.createdAt)
        assertEquals(now, post.updatedAt)
    }

    @Test
    fun `should throw exception when title is blank`() {
        // Given
        val id = PostId.generate()
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        assertThrows<IllegalArgumentException> {
            Post(
                id = id,
                title = "",
                content = "Content",
                categoryId = categoryId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when title exceeds 200 characters`() {
        // Given
        val id = PostId.generate()
        val longTitle = "a".repeat(201)
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        assertThrows<IllegalArgumentException> {
            Post(
                id = id,
                title = longTitle,
                content = "Content",
                categoryId = categoryId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when content is blank`() {
        // Given
        val id = PostId.generate()
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        assertThrows<IllegalArgumentException> {
            Post(
                id = id,
                title = "Title",
                content = "",
                categoryId = categoryId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when content exceeds 10000 characters`() {
        // Given
        val id = PostId.generate()
        val longContent = "a".repeat(10001)
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        assertThrows<IllegalArgumentException> {
            Post(
                id = id,
                title = "Title",
                content = longContent,
                categoryId = categoryId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should update title successfully`() {
        // Given
        val post = createTestPost()
        val newTitle = "Updated Title"

        // When
        val updatedPost = post.updateTitle(newTitle)

        // Then
        assertEquals(newTitle, updatedPost.title)
        assertEquals(post.id, updatedPost.id)
        assertEquals(post.content, updatedPost.content)
        assertEquals(post.categoryId, updatedPost.categoryId)
        assertEquals(post.createdAt, updatedPost.createdAt)
        assertNotEquals(post.updatedAt, updatedPost.updatedAt)
    }

    @Test
    fun `should update content successfully`() {
        // Given
        val post = createTestPost()
        val newContent = "Updated content"

        // When
        val updatedPost = post.updateContent(newContent)

        // Then
        assertEquals(newContent, updatedPost.content)
        assertEquals(post.id, updatedPost.id)
        assertEquals(post.title, updatedPost.title)
        assertEquals(post.categoryId, updatedPost.categoryId)
        assertEquals(post.createdAt, updatedPost.createdAt)
        assertNotEquals(post.updatedAt, updatedPost.updatedAt)
    }

    @Test
    fun `should update category successfully`() {
        // Given
        val post = createTestPost()
        val newCategoryId = CategoryId.generate()

        // When
        val updatedPost = post.updateCategory(newCategoryId)

        // Then
        assertEquals(newCategoryId, updatedPost.categoryId)
        assertEquals(post.id, updatedPost.id)
        assertEquals(post.title, updatedPost.title)
        assertEquals(post.content, updatedPost.content)
        assertEquals(post.createdAt, updatedPost.createdAt)
        assertNotEquals(post.updatedAt, updatedPost.updatedAt)
    }

    @Test
    fun `should throw exception when updating with blank title`() {
        // Given
        val post = createTestPost()

        // When & Then
        assertThrows<IllegalArgumentException> {
            post.updateTitle("")
        }
    }

    @Test
    fun `should throw exception when updating with blank content`() {
        // Given
        val post = createTestPost()

        // When & Then
        assertThrows<IllegalArgumentException> {
            post.updateContent("")
        }
    }

    private fun createTestPost(): Post {
        val now = LocalDateTime.now()
        return Post(
            id = PostId.generate(),
            title = "Test Post",
            content = "Test content",
            categoryId = CategoryId.generate(),
            createdAt = now,
            updatedAt = now
        )
    }
}

