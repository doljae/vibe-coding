package com.example.vibecoding.domain.post

import com.example.vibecoding.domain.category.CategoryId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

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
        post.id shouldBe id
        post.title shouldBe title
        post.content shouldBe content
        post.categoryId shouldBe categoryId
        post.createdAt shouldBe now
        post.updatedAt shouldBe now
    }

    @Test
    fun `should throw exception when title is blank`() {
        // Given
        val id = PostId.generate()
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        shouldThrow<IllegalArgumentException> {
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
        shouldThrow<IllegalArgumentException> {
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
        shouldThrow<IllegalArgumentException> {
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
        shouldThrow<IllegalArgumentException> {
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
        val post = createTestPost("Original Title", "Content")
        val newTitle = "Updated Title"

        // When
        val updatedPost = post.updateTitle(newTitle)

        // Then
        updatedPost.title shouldBe newTitle
        updatedPost.content shouldBe post.content
        updatedPost.categoryId shouldBe post.categoryId
        updatedPost.id shouldBe post.id
        updatedPost.createdAt shouldBe post.createdAt
        updatedPost.updatedAt shouldNotBe post.updatedAt
    }

    @Test
    fun `should update content successfully`() {
        // Given
        val post = createTestPost("Title", "Original content")
        val newContent = "Updated content"

        // When
        val updatedPost = post.updateContent(newContent)

        // Then
        updatedPost.content shouldBe newContent
        updatedPost.title shouldBe post.title
        updatedPost.categoryId shouldBe post.categoryId
        updatedPost.id shouldBe post.id
        updatedPost.createdAt shouldBe post.createdAt
        updatedPost.updatedAt shouldNotBe post.updatedAt
    }

    @Test
    fun `should update category successfully`() {
        // Given
        val post = createTestPost("Title", "Content")
        val newCategoryId = CategoryId.generate()

        // When
        val updatedPost = post.updateCategory(newCategoryId)

        // Then
        updatedPost.categoryId shouldBe newCategoryId
        updatedPost.title shouldBe post.title
        updatedPost.content shouldBe post.content
        updatedPost.id shouldBe post.id
        updatedPost.createdAt shouldBe post.createdAt
        updatedPost.updatedAt shouldNotBe post.updatedAt
    }

    @Test
    fun `should generate unique post IDs`() {
        // When
        val id1 = PostId.generate()
        val id2 = PostId.generate()

        // Then
        id1 shouldNotBe id2
    }

    @Test
    fun `should create post ID from string`() {
        // Given
        val uuidString = "123e4567-e89b-12d3-a456-426614174000"

        // When
        val postId = PostId.from(uuidString)

        // Then
        postId.value.toString() shouldBe uuidString
    }

    private fun createTestPost(title: String, content: String): Post {
        val now = LocalDateTime.now()
        return Post(
            id = PostId.generate(),
            title = title,
            content = content,
            categoryId = CategoryId.generate(),
            createdAt = now,
            updatedAt = now
        )
    }
}

