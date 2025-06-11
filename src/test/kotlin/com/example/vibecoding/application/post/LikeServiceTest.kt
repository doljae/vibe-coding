package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.*
import com.example.vibecoding.domain.user.UserId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class LikeServiceTest {

    private lateinit var likeService: LikeService
    private lateinit var likeRepository: LikeRepository
    private lateinit var postRepository: PostRepository

    @BeforeEach
    fun setUp() {
        likeRepository = mockk()
        postRepository = mockk()
        likeService = LikeService(likeRepository, postRepository)
    }

    @Test
    fun `should like post successfully`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId, likeCount = 5)
        val expectedLike = Like(
            id = LikeId.generate(),
            postId = postId,
            userId = userId,
            createdAt = LocalDateTime.now()
        )

        every { postRepository.findById(postId) } returns post
        every { likeRepository.existsByPostIdAndUserId(postId, userId) } returns false
        every { likeRepository.save(any()) } returns expectedLike
        every { postRepository.save(any()) } returns post.incrementLikeCount()

        // When
        val result = likeService.likePost(postId, userId)

        // Then
        result.postId shouldBe postId
        result.userId shouldBe userId
        verify { likeRepository.save(any()) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should throw exception when liking non-existent post`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()

        every { postRepository.findById(postId) } returns null

        // When & Then
        shouldThrow<PostNotFoundException> {
            likeService.likePost(postId, userId)
        }.message shouldBe "Post with id '$postId' not found"
    }

    @Test
    fun `should throw exception when user already liked post`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId)

        every { postRepository.findById(postId) } returns post
        every { likeRepository.existsByPostIdAndUserId(postId, userId) } returns true

        // When & Then
        shouldThrow<DuplicateLikeException> {
            likeService.likePost(postId, userId)
        }.message shouldBe "User '$userId' has already liked post '$postId'"
    }

    @Test
    fun `should unlike post successfully`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId, likeCount = 5)
        val existingLike = Like(
            id = LikeId.generate(),
            postId = postId,
            userId = userId,
            createdAt = LocalDateTime.now()
        )

        every { postRepository.findById(postId) } returns post
        every { likeRepository.findByPostIdAndUserId(postId, userId) } returns existingLike
        every { likeRepository.delete(existingLike.id) } returns true
        every { postRepository.save(any()) } returns post.decrementLikeCount()

        // When
        likeService.unlikePost(postId, userId)

        // Then
        verify { likeRepository.delete(existingLike.id) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should throw exception when unliking non-existent post`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()

        every { postRepository.findById(postId) } returns null

        // When & Then
        shouldThrow<PostNotFoundException> {
            likeService.unlikePost(postId, userId)
        }.message shouldBe "Post with id '$postId' not found"
    }

    @Test
    fun `should throw exception when unliking post that user hasn't liked`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId)

        every { postRepository.findById(postId) } returns post
        every { likeRepository.findByPostIdAndUserId(postId, userId) } returns null

        // When & Then
        shouldThrow<LikeNotFoundException> {
            likeService.unlikePost(postId, userId)
        }.message shouldBe "User '$userId' has not liked post '$postId'"
    }

    @Test
    fun `should toggle like from unliked to liked`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId)
        val expectedLike = Like(
            id = LikeId.generate(),
            postId = postId,
            userId = userId,
            createdAt = LocalDateTime.now()
        )

        every { likeRepository.existsByPostIdAndUserId(postId, userId) } returns false
        every { postRepository.findById(postId) } returns post
        every { likeRepository.save(any()) } returns expectedLike
        every { postRepository.save(any()) } returns post.incrementLikeCount()

        // When
        val result = likeService.toggleLike(postId, userId)

        // Then
        result shouldBe true
        verify { likeRepository.save(any()) }
    }

    @Test
    fun `should toggle like from liked to unliked`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId, likeCount = 1)
        val existingLike = Like(
            id = LikeId.generate(),
            postId = postId,
            userId = userId,
            createdAt = LocalDateTime.now()
        )

        every { likeRepository.existsByPostIdAndUserId(postId, userId) } returns true
        every { postRepository.findById(postId) } returns post
        every { likeRepository.findByPostIdAndUserId(postId, userId) } returns existingLike
        every { likeRepository.delete(existingLike.id) } returns true
        every { postRepository.save(any()) } returns post.decrementLikeCount()

        // When
        val result = likeService.toggleLike(postId, userId)

        // Then
        result shouldBe false
        verify { likeRepository.delete(existingLike.id) }
    }

    @Test
    fun `should check if user has liked post`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()

        every { likeRepository.existsByPostIdAndUserId(postId, userId) } returns true

        // When
        val result = likeService.hasUserLikedPost(postId, userId)

        // Then
        result shouldBe true
    }

    @Test
    fun `should get like count for post`() {
        // Given
        val postId = PostId.generate()
        val expectedCount = 10L

        every { likeRepository.countByPostId(postId) } returns expectedCount

        // When
        val result = likeService.getLikeCountForPost(postId)

        // Then
        result shouldBe expectedCount
    }

    @Test
    fun `should get likes for post`() {
        // Given
        val postId = PostId.generate()
        val likes = listOf(
            Like(LikeId.generate(), postId, UserId.generate(), LocalDateTime.now()),
            Like(LikeId.generate(), postId, UserId.generate(), LocalDateTime.now())
        )

        every { likeRepository.findByPostId(postId) } returns likes

        // When
        val result = likeService.getLikesForPost(postId)

        // Then
        result shouldHaveSize 2
        result shouldBe likes
    }

    @Test
    fun `should get likes by user`() {
        // Given
        val userId = UserId.generate()
        val likes = listOf(
            Like(LikeId.generate(), PostId.generate(), userId, LocalDateTime.now()),
            Like(LikeId.generate(), PostId.generate(), userId, LocalDateTime.now())
        )

        every { likeRepository.findByUserId(userId) } returns likes

        // When
        val result = likeService.getLikesByUser(userId)

        // Then
        result shouldHaveSize 2
        result shouldBe likes
    }

    @Test
    fun `should get like count by user`() {
        // Given
        val userId = UserId.generate()
        val expectedCount = 5L

        every { likeRepository.countByUserId(userId) } returns expectedCount

        // When
        val result = likeService.getLikeCountByUser(userId)

        // Then
        result shouldBe expectedCount
    }

    private fun createValidPost(
        postId: PostId = PostId.generate(),
        likeCount: Long = 0
    ): Post {
        return Post(
            id = postId,
            title = "Test Post",
            content = "Test content",
            authorId = UserId.generate(),
            categoryId = CategoryId.generate(),
            likeCount = likeCount,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}

