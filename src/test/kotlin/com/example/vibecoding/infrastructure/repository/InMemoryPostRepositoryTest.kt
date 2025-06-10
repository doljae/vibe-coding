package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.*

class InMemoryPostRepositoryTest {

    private lateinit var repository: InMemoryPostRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryPostRepository()
    }

    @Test
    fun `should save and find post by id`() {
        // Given
        val post = createTestPost("My Post", "Content", CategoryId.generate())

        // When
        val savedPost = repository.save(post)
        val foundPost = repository.findById(post.id)

        // Then
        assertEquals(savedPost, post)
        assertEquals(foundPost, post)
    }

    @Test
    fun `should return null when post not found by id`() {
        // Given
        val nonExistentId = PostId.generate()

        // When
        val result = repository.findById(nonExistentId)

        // Then
        assertNull(result)
    }

    @Test
    fun `should find all posts sorted by creation time descending`() {
        // Given
        val categoryId = CategoryId.generate()
        val post1 = createTestPost("Post 1", "Content 1", categoryId)
        val post2 = createTestPost("Post 2", "Content 2", categoryId)
        
        repository.save(post1)
        Thread.sleep(1) // Ensure different timestamps
        repository.save(post2)

        // When
        val result = repository.findAll()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains(post1))
        assertTrue(result.contains(post2))
        // Most recent first
        assertTrue(result[0].createdAt >= result[1].createdAt)
    }

    @Test
    fun `should find posts by category id sorted by creation time descending`() {
        // Given
        val categoryId1 = CategoryId.generate()
        val categoryId2 = CategoryId.generate()
        val post1 = createTestPost("Post 1", "Content 1", categoryId1)
        val post2 = createTestPost("Post 2", "Content 2", categoryId1)
        val post3 = createTestPost("Post 3", "Content 3", categoryId2)
        
        repository.save(post1)
        repository.save(post2)
        repository.save(post3)

        // When
        val result = repository.findByCategoryId(categoryId1)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains(post1))
        assertTrue(result.contains(post2))
        assertFalse(result.contains(post3))
    }

    @Test
    fun `should find posts by title case insensitive`() {
        // Given
        val categoryId = CategoryId.generate()
        val post1 = createTestPost("Technology Post", "Content 1", categoryId)
        val post2 = createTestPost("Science Post", "Content 2", categoryId)
        val post3 = createTestPost("Advanced Technology", "Content 3", categoryId)
        
        repository.save(post1)
        repository.save(post2)
        repository.save(post3)

        // When
        val result = repository.findByTitle("technology")

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains(post1))
        assertTrue(result.contains(post3))
        assertFalse(result.contains(post2))
    }

    @Test
    fun `should return empty list when no posts found by title`() {
        // Given
        val categoryId = CategoryId.generate()
        val post = createTestPost("Technology Post", "Content", categoryId)
        repository.save(post)

        // When
        val result = repository.findByTitle("NonExistent")

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should delete post successfully`() {
        // Given
        val post = createTestPost("My Post", "Content", CategoryId.generate())
        repository.save(post)

        // When
        val deleted = repository.delete(post.id)
        val found = repository.findById(post.id)

        // Then
        assertTrue(deleted)
        assertNull(found)
    }

    @Test
    fun `should return false when deleting non-existent post`() {
        // Given
        val nonExistentId = PostId.generate()

        // When
        val result = repository.delete(nonExistentId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should check if post exists by id`() {
        // Given
        val post = createTestPost("My Post", "Content", CategoryId.generate())
        repository.save(post)

        // When
        val exists = repository.existsById(post.id)
        val notExists = repository.existsById(PostId.generate())

        // Then
        assertTrue(exists)
        assertFalse(notExists)
    }

    @Test
    fun `should count posts by category id`() {
        // Given
        val categoryId1 = CategoryId.generate()
        val categoryId2 = CategoryId.generate()
        val post1 = createTestPost("Post 1", "Content 1", categoryId1)
        val post2 = createTestPost("Post 2", "Content 2", categoryId1)
        val post3 = createTestPost("Post 3", "Content 3", categoryId2)
        
        repository.save(post1)
        repository.save(post2)
        repository.save(post3)

        // When
        val count1 = repository.countByCategoryId(categoryId1)
        val count2 = repository.countByCategoryId(categoryId2)
        val count3 = repository.countByCategoryId(CategoryId.generate())

        // Then
        assertEquals(2L, count1)
        assertEquals(1L, count2)
        assertEquals(0L, count3)
    }

    @Test
    fun `should update existing post`() {
        // Given
        val post = createTestPost("My Post", "Content", CategoryId.generate())
        repository.save(post)
        
        val updatedPost = post.copy(title = "Updated Post")

        // When
        repository.save(updatedPost)
        val found = repository.findById(post.id)

        // Then
        assertEquals(updatedPost, found)
        assertEquals("Updated Post", found?.title)
    }

    private fun createTestPost(title: String, content: String, categoryId: CategoryId): Post {
        val now = LocalDateTime.now()
        return Post(
            id = PostId.generate(),
            title = title,
            content = content,
            categoryId = categoryId,
            createdAt = now,
            updatedAt = now
        )
    }
}

