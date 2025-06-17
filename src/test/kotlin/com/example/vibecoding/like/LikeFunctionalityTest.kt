package com.example.vibecoding.like

import com.example.vibecoding.application.post.LikeService
import com.example.vibecoding.domain.post.Like
import com.example.vibecoding.domain.post.LikeRepository
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.infrastructure.repository.InMemoryLikeRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID

/**
 * Functional tests for the like functionality
 * Tests the complete flow of like operations
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LikeFunctionalityTest {

    private lateinit var likeService: LikeService
    private lateinit var likeRepository: LikeRepository
    private lateinit var postRepository: PostRepository

    private val userId = UserId(UUID.randomUUID())
    private val postId = PostId(UUID.randomUUID())

    @BeforeEach
    fun setUp() {
        likeRepository = InMemoryLikeRepository()
        postRepository = mockk()
        likeService = LikeService(likeRepository, postRepository)

        // Mock post existence
        every { postRepository.findById(any()) } returns mockk(relaxed = true)
        every { postRepository.save(any()) } returnsArgument 0
    }

    @Test
    fun `should like and unlike posts correctly`() {
        // Given - Initial state with no likes
        likeService.getLikeCountForPost(postId) shouldBe 0
        likeService.hasUserLikedPost(postId, userId) shouldBe false

        // When - User likes the post
        val like = likeService.likePost(postId, userId)

        // Then - Verify like was created
        like.postId shouldBe postId
        like.userId shouldBe userId
        likeService.getLikeCountForPost(postId) shouldBe 1
        likeService.hasUserLikedPost(postId, userId) shouldBe true

        // When - User unlikes the post
        likeService.unlikePost(postId, userId)

        // Then - Verify like was removed
        likeService.getLikeCountForPost(postId) shouldBe 0
        likeService.hasUserLikedPost(postId, userId) shouldBe false
    }

    @Test
    fun `should toggle likes correctly`() {
        // Given - Initial state with no likes
        likeService.hasUserLikedPost(postId, userId) shouldBe false

        // When - Toggle like (first time = like)
        val firstToggle = likeService.toggleLike(postId, userId)

        // Then - Verify post is now liked
        firstToggle shouldBe true
        likeService.hasUserLikedPost(postId, userId) shouldBe true

        // When - Toggle like again (second time = unlike)
        val secondToggle = likeService.toggleLike(postId, userId)

        // Then - Verify post is now unliked
        secondToggle shouldBe false
        likeService.hasUserLikedPost(postId, userId) shouldBe false
    }

    @Test
    fun `should handle multiple users liking the same post`() {
        // Given - Multiple users
        val user1 = userId
        val user2 = UserId(UUID.randomUUID())
        val user3 = UserId(UUID.randomUUID())

        // When - All users like the post
        likeService.likePost(postId, user1)
        likeService.likePost(postId, user2)
        likeService.likePost(postId, user3)

        // Then - Verify like count and individual likes
        likeService.getLikeCountForPost(postId) shouldBe 3
        likeService.hasUserLikedPost(postId, user1) shouldBe true
        likeService.hasUserLikedPost(postId, user2) shouldBe true
        likeService.hasUserLikedPost(postId, user3) shouldBe true

        // When - One user unlikes
        likeService.unlikePost(postId, user2)

        // Then - Verify updated state
        likeService.getLikeCountForPost(postId) shouldBe 2
        likeService.hasUserLikedPost(postId, user1) shouldBe true
        likeService.hasUserLikedPost(postId, user2) shouldBe false
        likeService.hasUserLikedPost(postId, user3) shouldBe true
    }

    @Test
    fun `should handle one user liking multiple posts`() {
        // Given - Multiple posts
        val post1 = postId
        val post2 = PostId(UUID.randomUUID())
        val post3 = PostId(UUID.randomUUID())

        // Mock additional posts
        every { postRepository.findById(post2) } returns mockk(relaxed = true)
        every { postRepository.findById(post3) } returns mockk(relaxed = true)

        // When - User likes all posts
        likeService.likePost(post1, userId)
        likeService.likePost(post2, userId)
        likeService.likePost(post3, userId)

        // Then - Verify likes for each post
        likeService.getLikeCountForPost(post1) shouldBe 1
        likeService.getLikeCountForPost(post2) shouldBe 1
        likeService.getLikeCountForPost(post3) shouldBe 1
        likeService.getLikeCountByUser(userId) shouldBe 3

        // When - User unlikes one post
        likeService.unlikePost(post2, userId)

        // Then - Verify updated state
        likeService.getLikeCountForPost(post1) shouldBe 1
        likeService.getLikeCountForPost(post2) shouldBe 0
        likeService.getLikeCountForPost(post3) shouldBe 1
        likeService.getLikeCountByUser(userId) shouldBe 2
    }
}

