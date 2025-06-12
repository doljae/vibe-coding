package com.example.vibecoding.domain.post

import com.example.vibecoding.domain.user.UserId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class LikeTest {

    @Test
    fun `should create like with valid data`() {
        // Given
        val id = LikeId.generate()
        val postId = PostId.generate()
        val userId = UserId.generate()
        val now = LocalDateTime.now()

        // When
        val like = Like(
            id = id,
            postId = postId,
            userId = userId,
            createdAt = now
        )

        // Then
        like.id shouldBe id
        like.postId shouldBe postId
        like.userId shouldBe userId
        like.createdAt shouldBe now
    }

    @Test
    fun `should not allow future creation date`() {
        // Given
        val id = LikeId.generate()
        val postId = PostId.generate()
        val userId = UserId.generate()
        val futureDate = LocalDateTime.now().plusDays(1)

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Like(
                id = id,
                postId = postId,
                userId = userId,
                createdAt = futureDate
            )
        }.message shouldBe "Like creation date cannot be in the future"
    }

    @Test
    fun `should allow current time as creation date`() {
        // Given
        val id = LikeId.generate()
        val postId = PostId.generate()
        val userId = UserId.generate()
        val now = LocalDateTime.now()

        // When
        val like = Like(
            id = id,
            postId = postId,
            userId = userId,
            createdAt = now
        )

        // Then
        like.createdAt shouldBe now
    }

    @Test
    fun `should allow past creation date`() {
        // Given
        val id = LikeId.generate()
        val postId = PostId.generate()
        val userId = UserId.generate()
        val pastDate = LocalDateTime.now().minusDays(1)

        // When
        val like = Like(
            id = id,
            postId = postId,
            userId = userId,
            createdAt = pastDate
        )

        // Then
        like.createdAt shouldBe pastDate
    }

    @Test
    fun `should generate unique like IDs`() {
        // When
        val id1 = LikeId.generate()
        val id2 = LikeId.generate()

        // Then
        id1 shouldNotBe id2
        id1.value shouldNotBe id2.value
    }

    @Test
    fun `should create like ID from string`() {
        // Given
        val uuidString = "123e4567-e89b-12d3-a456-426614174000"

        // When
        val likeId = LikeId.from(uuidString)

        // Then
        likeId.value.toString() shouldBe uuidString
    }

    @Test
    fun `should throw exception for invalid UUID string`() {
        // Given
        val invalidUuidString = "invalid-uuid"

        // When & Then
        shouldThrow<IllegalArgumentException> {
            LikeId.from(invalidUuidString)
        }
    }

    @Test
    fun `two likes with same data should be equal`() {
        // Given
        val id = LikeId.generate()
        val postId = PostId.generate()
        val userId = UserId.generate()
        val now = LocalDateTime.now()

        val like1 = Like(id, postId, userId, now)
        val like2 = Like(id, postId, userId, now)

        // Then
        like1 shouldBe like2
        like1.hashCode() shouldBe like2.hashCode()
    }

    @Test
    fun `two likes with different IDs should not be equal`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val now = LocalDateTime.now()

        val like1 = Like(LikeId.generate(), postId, userId, now)
        val like2 = Like(LikeId.generate(), postId, userId, now)

        // Then
        like1 shouldNotBe like2
    }
}

