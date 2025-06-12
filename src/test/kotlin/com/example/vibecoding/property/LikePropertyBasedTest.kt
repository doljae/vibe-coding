package com.example.vibecoding.property

import com.example.vibecoding.application.post.DuplicateLikeException
import com.example.vibecoding.application.post.LikeService
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.*
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.infrastructure.repository.InMemoryLikeRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * Property-based tests for the Like feature using Kotest property testing
 * These tests generate random inputs to discover edge cases and verify invariants
 */
class LikePropertyBasedTest {

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
    fun `property - like count should always equal number of unique user-post combinations`() {
        checkAll(
            iterations = 50,
            Arb.list(Arb.pair(arbUserId(), arbPostId()), 1..20)
        ) { userPostPairs ->
            // Given - Reset repository for each test
            likeRepository.clear()
            
            val uniquePairs = userPostPairs.toSet()
            val posts = uniquePairs.map { (_, postId) -> createValidPost(postId = postId) }.distinctBy { it.id }
            
            // Mock all posts
            posts.forEach { post ->
                every { postRepository.findById(post.id) } returns post
                every { postRepository.save(any()) } returnsArgument 0
            }

            // When - Like all unique combinations
            uniquePairs.forEach { (userId, postId) ->
                likeService.likePost(postId, userId)
            }

            // Then - Repository size should equal unique combinations
            likeRepository.size() shouldBe uniquePairs.size

            // And like counts should be consistent
            posts.forEach { post ->
                val expectedCount = uniquePairs.count { it.second == post.id }
                likeService.getLikeCountForPost(post.id) shouldBe expectedCount.toLong()
            }
        }
    }

    @Test
    fun `property - duplicate likes should always be rejected`() {
        checkAll(
            iterations = 30,
            arbUserId(),
            arbPostId(),
            Arb.int(2..10)
        ) { userId, postId, attempts ->
            // Given
            likeRepository.clear()
            val post = createValidPost(postId = postId)
            
            every { postRepository.findById(postId) } returns post
            every { postRepository.save(any()) } returnsArgument 0

            // When - First like should succeed
            likeService.likePost(postId, userId)

            // Then - All subsequent attempts should fail
            repeat(attempts - 1) {
                shouldThrow<DuplicateLikeException> {
                    likeService.likePost(postId, userId)
                }
            }

            // And repository should contain exactly one like
            likeRepository.size() shouldBe 1
            likeService.getLikeCountForPost(postId) shouldBe 1
        }
    }

    @Test
    fun `property - toggle operations should maintain correct state`() {
        checkAll(
            iterations = 30,
            arbUserId(),
            arbPostId(),
            Arb.list(Arb.boolean(), 1..20)
        ) { userId, postId, toggleSequence ->
            // Given
            likeRepository.clear()
            val post = createValidPost(postId = postId)
            
            every { postRepository.findById(postId) } returns post
            every { postRepository.save(any()) } returnsArgument 0

            var expectedLiked = false

            // When - Apply toggle sequence
            toggleSequence.forEach { _ ->
                val result = likeService.toggleLike(postId, userId)
                expectedLiked = !expectedLiked
                
                // Then - Result should match expected state
                result shouldBe expectedLiked
            }

            // Final state verification
            likeService.hasUserLikedPost(postId, userId) shouldBe expectedLiked
            val expectedCount = if (expectedLiked) 1L else 0L
            likeService.getLikeCountForPost(postId) shouldBe expectedCount
        }
    }

    @Test
    fun `property - user like count should equal sum of their likes across all posts`() {
        checkAll(
            iterations = 30,
            arbUserId(),
            Arb.list(arbPostId(), 1..15)
        ) { userId, postIds ->
            // Given
            likeRepository.clear()
            val uniquePostIds = postIds.toSet()
            val posts = uniquePostIds.map { createValidPost(postId = it) }
            
            posts.forEach { post ->
                every { postRepository.findById(post.id) } returns post
                every { postRepository.save(any()) } returnsArgument 0
            }

            // When - User likes all posts
            uniquePostIds.forEach { postId ->
                likeService.likePost(postId, userId)
            }

            // Then - User's like count should equal number of unique posts
            likeService.getLikeCountByUser(userId) shouldBe uniquePostIds.size.toLong()
            
            // And each post should have exactly one like
            uniquePostIds.forEach { postId ->
                likeService.getLikeCountForPost(postId) shouldBe 1
            }
        }
    }

    @Test
    fun `property - like and unlike operations should be inverse operations`() {
        checkAll(
            iterations = 50,
            arbUserId(),
            arbPostId()
        ) { userId, postId ->
            // Given
            likeRepository.clear()
            val post = createValidPost(postId = postId)
            
            every { postRepository.findById(postId) } returns post
            every { postRepository.save(any()) } returnsArgument 0

            // Initial state
            val initialLiked = likeService.hasUserLikedPost(postId, userId)
            val initialCount = likeService.getLikeCountForPost(postId)

            // When - Like then unlike
            if (!initialLiked) {
                likeService.likePost(postId, userId)
                likeService.unlikePost(postId, userId)
            } else {
                // If somehow already liked, just unlike then like
                likeService.unlikePost(postId, userId)
                likeService.likePost(postId, userId)
            }

            // Then - Should return to initial state
            likeService.hasUserLikedPost(postId, userId) shouldBe initialLiked
            likeService.getLikeCountForPost(postId) shouldBe initialCount
        }
    }

    @Test
    fun `property - multiple users liking same post should accumulate correctly`() {
        checkAll(
            iterations = 30,
            arbPostId(),
            Arb.set(arbUserId(), 1..20)
        ) { postId, userIds ->
            // Given
            likeRepository.clear()
            val post = createValidPost(postId = postId)
            
            every { postRepository.findById(postId) } returns post
            every { postRepository.save(any()) } returnsArgument 0

            // When - Each user likes the post
            userIds.forEach { userId ->
                likeService.likePost(postId, userId)
            }

            // Then - Like count should equal number of users
            likeService.getLikeCountForPost(postId) shouldBe userIds.size.toLong()
            
            // And each user should have liked the post
            userIds.forEach { userId ->
                likeService.hasUserLikedPost(postId, userId) shouldBe true
            }
            
            // And repository should contain exactly the right number of likes
            likeRepository.size() shouldBe userIds.size
        }
    }

    @Test
    fun `property - like operations should preserve data integrity under random operations`() {
        checkAll(
            iterations = 20,
            Arb.list(
                Arb.choice(
                    arbLikeOperation(),
                    arbUnlikeOperation(),
                    arbToggleOperation()
                ),
                5..30
            )
        ) { operations ->
            // Given
            likeRepository.clear()
            val posts = (1..5).map { createValidPost() }
            val users = (1..5).map { UserId.generate() }
            
            posts.forEach { post ->
                every { postRepository.findById(post.id) } returns post
                every { postRepository.save(any()) } returnsArgument 0
            }

            val expectedState = mutableSetOf<Pair<UserId, PostId>>()

            // When - Apply random operations
            operations.forEach { operation ->
                val userId = users.random()
                val postId = posts.random().id
                val pair = userId to postId

                try {
                    when (operation) {
                        is LikeOperation -> {
                            if (pair !in expectedState) {
                                likeService.likePost(postId, userId)
                                expectedState.add(pair)
                            }
                        }
                        is UnlikeOperation -> {
                            if (pair in expectedState) {
                                likeService.unlikePost(postId, userId)
                                expectedState.remove(pair)
                            }
                        }
                        is ToggleOperation -> {
                            val result = likeService.toggleLike(postId, userId)
                            if (result) {
                                expectedState.add(pair)
                            } else {
                                expectedState.remove(pair)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Some operations may fail due to invalid state, which is expected
                }
            }

            // Then - Final state should match expected state
            likeRepository.size() shouldBe expectedState.size
            
            expectedState.forEach { (userId, postId) ->
                likeService.hasUserLikedPost(postId, userId) shouldBe true
            }
        }
    }

    // Custom arbitraries for generating test data
    private fun arbUserId(): Arb<UserId> = Arb.uuid().map { UserId.from(it.toString()) }
    
    private fun arbPostId(): Arb<PostId> = Arb.uuid().map { PostId.from(it.toString()) }

    private fun arbLikeOperation(): Arb<LikeOperation> = Arb.constant(LikeOperation)
    
    private fun arbUnlikeOperation(): Arb<UnlikeOperation> = Arb.constant(UnlikeOperation)
    
    private fun arbToggleOperation(): Arb<ToggleOperation> = Arb.constant(ToggleOperation)

    private fun createValidPost(
        postId: PostId = PostId.generate(),
        likeCount: Long = 0
    ): Post {
        return Post(
            id = postId,
            title = "Property Test Post",
            content = "Content for property-based testing",
            authorId = UserId.generate(),
            categoryId = CategoryId.generate(),
            likeCount = likeCount,
            createdAt = LocalDateTime.now().minusHours(1),
            updatedAt = LocalDateTime.now()
        )
    }

    // Operation types for random testing
    sealed class Operation
    object LikeOperation : Operation()
    object UnlikeOperation : Operation()
    object ToggleOperation : Operation()
}

