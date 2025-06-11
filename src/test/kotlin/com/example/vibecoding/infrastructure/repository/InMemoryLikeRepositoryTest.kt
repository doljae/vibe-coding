package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.post.Like
import com.example.vibecoding.domain.post.LikeId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class InMemoryLikeRepositoryTest {

    private lateinit var repository: InMemoryLikeRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryLikeRepository()
    }

    @Test
    fun `should save and find like by id`() {
        // Given
        val like = createValidLike()

        // When
        val savedLike = repository.save(like)
        val foundLike = repository.findById(like.id)

        // Then
        savedLike shouldBe like
        foundLike shouldBe like
    }

    @Test
    fun `should return null when like not found by id`() {
        // Given
        val nonExistentId = LikeId.generate()

        // When
        val foundLike = repository.findById(nonExistentId)

        // Then
        foundLike shouldBe null
    }

    @Test
    fun `should find likes by post id`() {
        // Given
        val postId = PostId.generate()
        val like1 = createValidLike(postId = postId)
        val like2 = createValidLike(postId = postId)
        val like3 = createValidLike() // different post

        repository.save(like1)
        repository.save(like2)
        repository.save(like3)

        // When
        val likesForPost = repository.findByPostId(postId)

        // Then
        likesForPost shouldHaveSize 2
        likesForPost shouldContain like1
        likesForPost shouldContain like2
    }

    @Test
    fun `should find likes by user id`() {
        // Given
        val userId = UserId.generate()
        val like1 = createValidLike(userId = userId)
        val like2 = createValidLike(userId = userId)
        val like3 = createValidLike() // different user

        repository.save(like1)
        repository.save(like2)
        repository.save(like3)

        // When
        val likesByUser = repository.findByUserId(userId)

        // Then
        likesByUser shouldHaveSize 2
        likesByUser shouldContain like1
        likesByUser shouldContain like2
    }

    @Test
    fun `should find like by post id and user id`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val like = createValidLike(postId = postId, userId = userId)
        val otherLike = createValidLike()

        repository.save(like)
        repository.save(otherLike)

        // When
        val foundLike = repository.findByPostIdAndUserId(postId, userId)

        // Then
        foundLike shouldBe like
    }

    @Test
    fun `should return null when like not found by post id and user id`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()

        // When
        val foundLike = repository.findByPostIdAndUserId(postId, userId)

        // Then
        foundLike shouldBe null
    }

    @Test
    fun `should delete like by id`() {
        // Given
        val like = createValidLike()
        repository.save(like)

        // When
        val deleted = repository.delete(like.id)
        val foundLike = repository.findById(like.id)

        // Then
        deleted shouldBe true
        foundLike shouldBe null
    }

    @Test
    fun `should return false when deleting non-existent like`() {
        // Given
        val nonExistentId = LikeId.generate()

        // When
        val deleted = repository.delete(nonExistentId)

        // Then
        deleted shouldBe false
    }

    @Test
    fun `should delete like by post id and user id`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val like = createValidLike(postId = postId, userId = userId)
        repository.save(like)

        // When
        val deleted = repository.deleteByPostIdAndUserId(postId, userId)
        val foundLike = repository.findByPostIdAndUserId(postId, userId)

        // Then
        deleted shouldBe true
        foundLike shouldBe null
    }

    @Test
    fun `should return false when deleting non-existent like by post id and user id`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()

        // When
        val deleted = repository.deleteByPostIdAndUserId(postId, userId)

        // Then
        deleted shouldBe false
    }

    @Test
    fun `should check if like exists by post id and user id`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val like = createValidLike(postId = postId, userId = userId)
        repository.save(like)

        // When & Then
        repository.existsByPostIdAndUserId(postId, userId) shouldBe true
        repository.existsByPostIdAndUserId(PostId.generate(), userId) shouldBe false
        repository.existsByPostIdAndUserId(postId, UserId.generate()) shouldBe false
    }

    @Test
    fun `should count likes by post id`() {
        // Given
        val postId = PostId.generate()
        val like1 = createValidLike(postId = postId)
        val like2 = createValidLike(postId = postId)
        val like3 = createValidLike() // different post

        repository.save(like1)
        repository.save(like2)
        repository.save(like3)

        // When
        val count = repository.countByPostId(postId)

        // Then
        count shouldBe 2
    }

    @Test
    fun `should count likes by user id`() {
        // Given
        val userId = UserId.generate()
        val like1 = createValidLike(userId = userId)
        val like2 = createValidLike(userId = userId)
        val like3 = createValidLike() // different user

        repository.save(like1)
        repository.save(like2)
        repository.save(like3)

        // When
        val count = repository.countByUserId(userId)

        // Then
        count shouldBe 2
    }

    @Test
    fun `should return empty list when no likes found`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()

        // When & Then
        repository.findByPostId(postId).shouldBeEmpty()
        repository.findByUserId(userId).shouldBeEmpty()
    }

    @Test
    fun `should return zero count when no likes found`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()

        // When & Then
        repository.countByPostId(postId) shouldBe 0
        repository.countByUserId(userId) shouldBe 0
    }

    @Test
    fun `should clear all likes`() {
        // Given
        repository.save(createValidLike())
        repository.save(createValidLike())
        repository.size() shouldBe 2

        // When
        repository.clear()

        // Then
        repository.size() shouldBe 0
    }

    @Test
    fun `should return correct size`() {
        // Given
        repository.size() shouldBe 0

        // When
        repository.save(createValidLike())
        repository.save(createValidLike())

        // Then
        repository.size() shouldBe 2
    }

    @Test
    fun `should sort likes by creation date descending`() {
        // Given
        val now = LocalDateTime.now()
        val postId = PostId.generate()
        
        val like1 = createValidLike(postId = postId, createdAt = now.minusHours(2))
        val like2 = createValidLike(postId = postId, createdAt = now.minusHours(1))
        val like3 = createValidLike(postId = postId, createdAt = now)

        repository.save(like1)
        repository.save(like2)
        repository.save(like3)

        // When
        val likes = repository.findByPostId(postId)

        // Then
        likes shouldHaveSize 3
        likes[0] shouldBe like3 // most recent first
        likes[1] shouldBe like2
        likes[2] shouldBe like1 // oldest last
    }

    @Test
    fun `should update existing like when saving with same id`() {
        // Given
        val originalLike = createValidLike()
        repository.save(originalLike)

        val updatedLike = originalLike.copy(createdAt = LocalDateTime.now().plusMinutes(1))

        // When
        repository.save(updatedLike)
        val foundLike = repository.findById(originalLike.id)

        // Then
        foundLike shouldBe updatedLike
        foundLike shouldNotBe originalLike
        repository.size() shouldBe 1
    }

    private fun createValidLike(
        postId: PostId = PostId.generate(),
        userId: UserId = UserId.generate(),
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Like {
        return Like(
            id = LikeId.generate(),
            postId = postId,
            userId = userId,
            createdAt = createdAt
        )
    }
}

