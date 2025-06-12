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

    // ========== COMPLEX TEST SCENARIOS ==========

    @Test
    fun `should handle concurrent like attempts on same post by different users`() {
        // Given
        val postId = PostId.generate()
        val user1 = UserId.generate()
        val user2 = UserId.generate()
        val user3 = UserId.generate()
        val post = createValidPost(postId = postId, likeCount = 0)

        // Mock repository calls for concurrent scenario
        every { postRepository.findById(postId) } returns post
        every { likeRepository.existsByPostIdAndUserId(postId, user1) } returns false
        every { likeRepository.existsByPostIdAndUserId(postId, user2) } returns false
        every { likeRepository.existsByPostIdAndUserId(postId, user3) } returns false
        every { likeRepository.save(any()) } returnsMany listOf(
            createValidLike(postId = postId, userId = user1),
            createValidLike(postId = postId, userId = user2),
            createValidLike(postId = postId, userId = user3)
        )
        every { postRepository.save(any()) } returns post.copy(likeCount = 1) andThen 
                                                      post.copy(likeCount = 2) andThen 
                                                      post.copy(likeCount = 3)

        // When - Simulate concurrent likes
        val results = listOf(
            likeService.likePost(postId, user1),
            likeService.likePost(postId, user2),
            likeService.likePost(postId, user3)
        )

        // Then
        results shouldHaveSize 3
        results.forEach { like ->
            like.postId shouldBe postId
            like.userId shouldNotBe null
        }
        verify(exactly = 3) { likeRepository.save(any()) }
        verify(exactly = 3) { postRepository.save(any()) }
    }

    @Test
    fun `should handle like and unlike sequence by same user`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId, likeCount = 5)
        val like = createValidLike(postId = postId, userId = userId)

        every { postRepository.findById(postId) } returns post
        every { postRepository.save(any()) } returnsArgument 0

        // Test like operation
        every { likeRepository.existsByPostIdAndUserId(postId, userId) } returns false
        every { likeRepository.save(any()) } returns like
        
        // When
        val result = likeService.likePost(postId, userId)

        // Then
        result.postId shouldBe postId
        result.userId shouldBe userId
        verify(exactly = 1) { likeRepository.save(any()) }
        verify(exactly = 1) { postRepository.save(any()) }
    }

    @Test
    fun `should handle bulk like operations`() {
        // Given
        val post1 = createValidPost(likeCount = 1)
        val post2 = createValidPost(likeCount = 2)
        val userId = UserId.generate()
        val like1 = createValidLike(postId = post1.id, userId = userId)
        val like2 = createValidLike(postId = post2.id, userId = userId)

        // Mock for first post
        every { postRepository.findById(post1.id) } returns post1
        every { likeRepository.existsByPostIdAndUserId(post1.id, userId) } returns false
        every { likeRepository.save(match { it.postId == post1.id }) } returns like1
        every { postRepository.save(match { it.id == post1.id }) } returnsArgument 0

        // Mock for second post
        every { postRepository.findById(post2.id) } returns post2
        every { likeRepository.existsByPostIdAndUserId(post2.id, userId) } returns false
        every { likeRepository.save(match { it.postId == post2.id }) } returns like2
        every { postRepository.save(match { it.id == post2.id }) } returnsArgument 0

        // When
        val result1 = likeService.likePost(post1.id, userId)
        val result2 = likeService.likePost(post2.id, userId)

        // Then
        result1.postId shouldBe post1.id
        result1.userId shouldBe userId
        result2.postId shouldBe post2.id
        result2.userId shouldBe userId
        
        verify(exactly = 2) { likeRepository.save(any()) }
        verify(exactly = 2) { postRepository.save(any()) }
    }

    @Test
    fun `should handle edge case with post having maximum like count`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val maxLikeCount = Long.MAX_VALUE - 1
        val post = createValidPost(postId = postId, likeCount = maxLikeCount)
        val like = createValidLike(postId = postId, userId = userId)

        every { postRepository.findById(postId) } returns post
        every { likeRepository.existsByPostIdAndUserId(postId, userId) } returns false
        every { likeRepository.save(any()) } returns like
        every { postRepository.save(any()) } returns post.copy(likeCount = maxLikeCount + 1)

        // When
        val result = likeService.likePost(postId, userId)

        // Then
        result shouldNotBe null
        result.postId shouldBe postId
        result.userId shouldBe userId
        verify { postRepository.save(match { it.likeCount == maxLikeCount + 1 }) }
    }

    @Test
    fun `should handle toggle like operation`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId, likeCount = 10)
        val like = createValidLike(postId = postId, userId = userId)

        every { postRepository.findById(postId) } returns post
        every { postRepository.save(any()) } returnsArgument 0

        // Test toggle from not liked to liked
        every { likeRepository.existsByPostIdAndUserId(postId, userId) } returns false
        every { likeRepository.save(any()) } returns like

        // When
        val result = likeService.toggleLike(postId, userId)

        // Then
        result shouldBe true
        verify(exactly = 1) { likeRepository.save(any()) }
        verify(exactly = 1) { postRepository.save(any()) }
    }

    @Test
    fun `should handle user with extensive like history`() {
        // Given
        val userId = UserId.generate()
        val postCount = 100
        val posts = (1..postCount).map { createValidPost(likeCount = it.toLong()) }
        val likes = posts.map { createValidLike(postId = it.id, userId = userId) }

        every { likeRepository.findByUserId(userId) } returns likes
        every { likeRepository.countByUserId(userId) } returns postCount.toLong()

        // When
        val userLikes = likeService.getLikesByUser(userId)
        val likeCount = likeService.getLikeCountByUser(userId)

        // Then
        userLikes shouldHaveSize postCount
        likeCount shouldBe postCount.toLong()
        userLikes.forEach { like ->
            like.userId shouldBe userId
            like.postId shouldNotBe null
        }
    }

    @Test
    fun `should handle post with massive like count and verify performance`() {
        // Given
        val postId = PostId.generate()
        val massiveLikeCount = 1_000_000L
        val likes = (1..1000).map { 
            createValidLike(postId = postId, userId = UserId.generate()) 
        }

        every { likeRepository.findByPostId(postId) } returns likes
        every { likeRepository.countByPostId(postId) } returns massiveLikeCount

        // When
        val startTime = System.currentTimeMillis()
        val postLikes = likeService.getLikesForPost(postId)
        val likeCount = likeService.getLikeCountForPost(postId)
        val endTime = System.currentTimeMillis()

        // Then
        postLikes shouldHaveSize 1000
        likeCount shouldBe massiveLikeCount
        val executionTime = endTime - startTime
        // Verify reasonable performance (should complete within 100ms)
        assert(executionTime < 100) { "Operation took too long: ${executionTime}ms" }
    }

    @Test
    fun `should handle complex scenario with multiple users and posts interaction`() {
        // Given
        val users = (1..5).map { UserId.generate() }
        val posts = (1..3).map { createValidPost(likeCount = 0) }
        val allCombinations = users.flatMap { user -> posts.map { post -> user to post } }

        // Mock all combinations
        allCombinations.forEach { (userId, post) ->
            every { postRepository.findById(post.id) } returns post
            every { likeRepository.existsByPostIdAndUserId(post.id, userId) } returns false
            every { likeRepository.save(any()) } returns createValidLike(postId = post.id, userId = userId)
            every { postRepository.save(any()) } returns post.incrementLikeCount()
        }

        // When - Each user likes each post
        val results = allCombinations.map { (userId, post) ->
            likeService.likePost(post.id, userId)
        }

        // Then
        results shouldHaveSize 15 // 5 users × 3 posts
        results.forEach { like ->
            like.postId shouldNotBe null
            like.userId shouldNotBe null
        }

        // Verify each post was saved multiple times (once per like)
        posts.forEach { post ->
            verify(exactly = 5) { postRepository.save(match { it.id == post.id }) }
        }
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
            imageAttachments = emptyList(),
            likeCount = likeCount,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createValidLike(
        postId: PostId = PostId.generate(),
        userId: UserId = UserId.generate()
    ): Like {
        return Like(
            id = LikeId.generate(),
            postId = postId,
            userId = userId,
            createdAt = LocalDateTime.now()
        )
    }
}
