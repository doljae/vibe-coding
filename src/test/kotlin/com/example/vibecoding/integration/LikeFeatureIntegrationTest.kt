package com.example.vibecoding.integration

import com.example.vibecoding.application.post.DuplicateLikeException
import com.example.vibecoding.application.post.LikeNotFoundException
import com.example.vibecoding.application.post.LikeService
import com.example.vibecoding.application.post.PostNotFoundException
import com.example.vibecoding.application.post.PostService
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.*
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.infrastructure.repository.InMemoryLikeRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Integration tests for the Like feature
 * Tests the complete flow from service layer to repository layer
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LikeFeatureIntegrationTest {

    private lateinit var likeService: LikeService
    private lateinit var likeRepository: InMemoryLikeRepository
    private lateinit var postRepository: PostRepository

    @BeforeEach
    fun setUp() {
        likeRepository = InMemoryLikeRepository()
        postRepository = mockk()
        likeService = LikeService(likeRepository, postRepository)
    }

    @Test
    fun `should handle complete like workflow with real repository`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId, likeCount = 0)

        every { postRepository.findById(postId) } returns post
        every { postRepository.save(any()) } returnsArgument 0

        // When - Like the post
        val like = likeService.likePost(postId, userId)

        // Then - Verify like was created
        like.postId shouldBe postId
        like.userId shouldBe userId
        likeRepository.size() shouldBe 1

        // When - Check like status
        val hasLiked = likeService.hasUserLikedPost(postId, userId)
        val likeCount = likeService.getLikeCountForPost(postId)

        // Then - Verify status
        hasLiked shouldBe true
        likeCount shouldBe 1

        // When - Unlike the post
        likeService.unlikePost(postId, userId)

        // Then - Verify like was removed
        likeRepository.size() shouldBe 0
        likeService.hasUserLikedPost(postId, userId) shouldBe false
        likeService.getLikeCountForPost(postId) shouldBe 0
    }

    @Test
    fun `should handle concurrent likes with real repository and thread safety`() {
        // Given
        val postId = PostId.generate()
        val userCount = 50
        val users = (1..userCount).map { UserId.generate() }
        val post = createValidPost(postId = postId, likeCount = 0)

        every { postRepository.findById(postId) } returns post
        every { postRepository.save(any()) } returnsArgument 0

        val executor = Executors.newFixedThreadPool(10)

        try {
            // When - Multiple users like the same post concurrently
            val futures = users.map { userId ->
                CompletableFuture.supplyAsync({
                    try {
                        likeService.likePost(postId, userId)
                    } catch (e: Exception) {
                        null
                    }
                }, executor)
            }

            // Wait for all operations to complete
            val results = futures.map { it.get(5, TimeUnit.SECONDS) }

            // Then - Verify all likes were processed
            val successfulLikes = results.filterNotNull()
            successfulLikes shouldHaveSize userCount
            likeRepository.size() shouldBe userCount
            likeService.getLikeCountForPost(postId) shouldBe userCount.toLong()

            // Verify each user has liked the post
            users.forEach { userId ->
                likeService.hasUserLikedPost(postId, userId) shouldBe true
            }

        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun `should handle stress test with many posts and users`() {
        // Given
        val postCount = 20
        val userCount = 30
        val posts = (1..postCount).map { createValidPost(likeCount = 0) }
        val users = (1..userCount).map { UserId.generate() }

        // Mock all posts
        posts.forEach { post ->
            every { postRepository.findById(post.id) } returns post
        }
        every { postRepository.save(any()) } returnsArgument 0

        // When - Each user likes each post
        posts.forEach { post ->
            users.forEach { userId ->
                likeService.likePost(post.id, userId)
            }
        }

        // Then - Verify all likes were created
        val totalExpectedLikes = postCount * userCount
        likeRepository.size() shouldBe totalExpectedLikes

        // Verify like counts per post
        posts.forEach { post ->
            likeService.getLikeCountForPost(post.id) shouldBe userCount.toLong()
        }

        // Verify like counts per user
        users.forEach { userId ->
            likeService.getLikeCountByUser(userId) shouldBe postCount.toLong()
        }
    }

    @Test
    fun `should handle rapid toggle operations with real repository`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId, likeCount = 0)

        every { postRepository.findById(postId) } returns post
        every { postRepository.save(any()) } returnsArgument 0

        // When - Rapid toggle sequence
        val toggleResults = mutableListOf<Boolean>()
        repeat(10) {
            toggleResults.add(likeService.toggleLike(postId, userId))
        }

        // Then - Verify alternating pattern
        toggleResults.forEachIndexed { index, result ->
            val expected = index % 2 == 0 // true for even indices (0, 2, 4...), false for odd
            result shouldBe expected
        }

        // Final state should be unliked (10 toggles = even number)
        likeService.hasUserLikedPost(postId, userId) shouldBe false
        likeRepository.size() shouldBe 0
    }

    @Test
    fun `should maintain data consistency during complex operations`() {
        // Given
        val posts = (1..5).map { createValidPost(likeCount = 0) }
        val users = (1..10).map { UserId.generate() }

        posts.forEach { post ->
            every { postRepository.findById(post.id) } returns post
        }
        every { postRepository.save(any()) } returnsArgument 0

        // When - Complex sequence of operations
        // 1. All users like all posts
        posts.forEach { post ->
            users.forEach { userId ->
                likeService.likePost(post.id, userId)
            }
        }

        // 2. Half the users unlike half the posts
        val halfUsers = users.take(5)
        val halfPosts = posts.take(3)
        halfPosts.forEach { post ->
            halfUsers.forEach { userId ->
                likeService.unlikePost(post.id, userId)
            }
        }

        // 3. Some users toggle likes on remaining posts
        val remainingPosts = posts.drop(3)
        users.take(3).forEach { userId ->
            remainingPosts.forEach { post ->
                likeService.toggleLike(post.id, userId) // This should unlike
            }
        }

        // Then - Verify final state consistency
        // Posts 0-2: 5 likes each (10 - 5 = 5)
        halfPosts.forEach { post ->
            likeService.getLikeCountForPost(post.id) shouldBe 5
        }

        // Posts 3-4: 7 likes each (10 - 3 = 7)
        remainingPosts.forEach { post ->
            likeService.getLikeCountForPost(post.id) shouldBe 7
        }

        // Total likes should be consistent
        val totalLikes = posts.sumOf { likeService.getLikeCountForPost(it.id) }
        totalLikes shouldBe (5 * 3 + 7 * 2) // 15 + 14 = 29
        likeRepository.size() shouldBe 29
    }

    @Test
    fun `should handle edge cases with repository operations`() {
        // Given
        val postId = PostId.generate()
        val userId = UserId.generate()
        val post = createValidPost(postId = postId, likeCount = 0)

        every { postRepository.findById(postId) } returns post
        every { postRepository.save(any()) } returnsArgument 0

        // Test 1: Like non-existent post
        val nonExistentPostId = PostId.generate()
        every { postRepository.findById(nonExistentPostId) } returns null

        shouldThrow<PostNotFoundException> {
            likeService.likePost(nonExistentPostId, userId)
        }

        // Test 2: Unlike non-existent like
        shouldThrow<LikeNotFoundException> {
            likeService.unlikePost(postId, userId)
        }

        // Test 3: Duplicate like attempt
        likeService.likePost(postId, userId)
        shouldThrow<DuplicateLikeException> {
            likeService.likePost(postId, userId)
        }

        // Test 4: Repository state should remain consistent
        likeRepository.size() shouldBe 1
        likeService.getLikeCountForPost(postId) shouldBe 1
    }

    @Test
    fun `should handle performance requirements for large datasets`() {
        // Given
        val largePostCount = 100
        val largeUserCount = 100
        val posts = (1..largePostCount).map { createValidPost(likeCount = 0) }
        val users = (1..largeUserCount).map { UserId.generate() }

        posts.forEach { post ->
            every { postRepository.findById(post.id) } returns post
        }
        every { postRepository.save(any()) } returnsArgument 0

        // When - Measure performance of bulk operations
        val startTime = System.currentTimeMillis()

        // Create likes for first 50 posts only to keep test reasonable
        posts.take(50).forEach { post ->
            users.take(50).forEach { userId ->
                likeService.likePost(post.id, userId)
            }
        }

        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime

        // Then - Verify performance and correctness
        val expectedLikes = 50 * 50 // 2500 likes
        likeRepository.size() shouldBe expectedLikes

        // Performance should be reasonable (less than 5 seconds for 2500 operations)
        assert(executionTime < 5000) { "Bulk operations took too long: ${executionTime}ms" }

        // Verify data integrity
        posts.take(50).forEach { post ->
            likeService.getLikeCountForPost(post.id) shouldBe 50
        }
    }

    private fun createValidPost(
        postId: PostId = PostId.generate(),
        likeCount: Long = 0
    ): Post {
        return Post(
            id = postId,
            title = "Integration Test Post",
            content = "Content for integration testing",
            authorId = UserId.generate(),
            categoryId = CategoryId.generate(),
            likeCount = likeCount,
            createdAt = LocalDateTime.now().minusHours(1),
            updatedAt = LocalDateTime.now()
        )
    }
}
