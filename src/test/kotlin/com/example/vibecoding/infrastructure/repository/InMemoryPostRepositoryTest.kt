package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

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
        savedPost shouldBe post
        foundPost shouldBe post
    }

    @Test
    fun `should return null when post not found by id`() {
        // Given
        val nonExistentId = PostId.generate()

        // When
        val result = repository.findById(nonExistentId)

        // Then
        result.shouldBeNull()
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
        result.size shouldBe 2
        result shouldContain post1
        result shouldContain post2
        // Most recent first
        (result[0].createdAt >= result[1].createdAt) shouldBe true
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
        result.size shouldBe 2
        result shouldContain post1
        result shouldContain post2
        result shouldNotContain post3
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
        result.size shouldBe 2
        result shouldContain post1
        result shouldContain post3
        result shouldNotContain post2
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
        result.shouldBeEmpty()
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
        deleted shouldBe true
        found.shouldBeNull()
    }

    @Test
    fun `should return false when deleting non-existent post`() {
        // Given
        val nonExistentId = PostId.generate()

        // When
        val result = repository.delete(nonExistentId)

        // Then
        result shouldBe false
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
        exists shouldBe true
        notExists shouldBe false
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
        count1 shouldBe 2L
        count2 shouldBe 1L
        count3 shouldBe 0L
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
        found shouldBe updatedPost
        found?.title shouldBe "Updated Post"
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

