package com.example.vibecoding.integration

import com.example.vibecoding.application.comment.CommentNotFoundException
import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.application.comment.CommentWithReplies
import com.example.vibecoding.application.comment.UnauthorizedCommentModificationException
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import com.example.vibecoding.infrastructure.repository.InMemoryCommentRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Integration tests for the Comment feature
 * Tests the complete flow from service layer to repository layer
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentFeatureIntegrationTest {

    private lateinit var commentService: CommentService
    private lateinit var commentRepository: InMemoryCommentRepository
    private lateinit var postRepository: PostRepository
    private lateinit var userRepository: UserRepository

    private val userId1 = UserId.generate()
    private val userId2 = UserId.generate()
    private val postId = PostId.generate()
    private val validContent = "This is a valid comment"

    private lateinit var testUser1: User
    private lateinit var testUser2: User
    private lateinit var testPost: Post

    @BeforeEach
    fun setUp() {
        commentRepository = InMemoryCommentRepository()
        postRepository = mockk()
        userRepository = mockk()
        commentService = CommentService(commentRepository, postRepository, userRepository)

        testUser1 = User(
            id = userId1,
            username = "testuser1",
            email = "test1@example.com",
            displayName = "Test User 1",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        testUser2 = User(
            id = userId2,
            username = "testuser2",
            email = "test2@example.com",
            displayName = "Test User 2",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        testPost = Post(
            id = postId,
            title = "Test Post",
            content = "Test post content",
            authorId = userId1,
            categoryId = CategoryId.generate(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { postRepository.findById(postId) } returns testPost
        every { userRepository.findById(userId1) } returns testUser1
        every { userRepository.findById(userId2) } returns testUser2
    }

    @Test
    fun `should handle complete comment workflow with real repository`() {
        // Given - Setup is done in setUp()

        // When - Create a root comment
        val rootComment = commentService.createComment(validContent, userId1, postId)

        // Then - Verify comment was created
        rootComment.content shouldBe validContent
        rootComment.authorId shouldBe userId1
        rootComment.postId shouldBe postId
        rootComment.isRootComment() shouldBe true
        commentRepository.findAll() shouldHaveSize 1

        // When - Create a reply to the root comment
        val replyContent = "This is a reply to the root comment"
        val reply = commentService.createReply(replyContent, userId2, postId, rootComment.id)

        // Then - Verify reply was created
        reply.content shouldBe replyContent
        reply.authorId shouldBe userId2
        reply.postId shouldBe postId
        reply.parentCommentId shouldBe rootComment.id
        reply.isReply() shouldBe true
        commentRepository.findAll() shouldHaveSize 2

        // When - Get comments for post
        val commentsWithReplies = commentService.getCommentsForPost(postId)

        // Then - Verify comments structure
        commentsWithReplies shouldHaveSize 1
        commentsWithReplies[0].comment.id shouldBe rootComment.id
        commentsWithReplies[0].replies shouldHaveSize 1
        commentsWithReplies[0].replies[0].id shouldBe reply.id

        // When - Update the root comment
        val updatedContent = "Updated root comment"
        val updatedComment = commentService.updateComment(rootComment.id, updatedContent, userId1)

        // Then - Verify comment was updated
        updatedComment.content shouldBe updatedContent
        updatedComment.id shouldBe rootComment.id
        commentRepository.findById(rootComment.id)?.content shouldBe updatedContent

        // When - Try to update comment with unauthorized user
        val exception = shouldThrow<UnauthorizedCommentModificationException> {
            commentService.updateComment(rootComment.id, "Unauthorized update", userId2)
        }

        // Then - Verify exception
        exception.message shouldBe "User is not authorized to modify this comment"

        // When - Delete the reply
        commentService.deleteComment(reply.id, userId2)

        // Then - Verify reply was deleted
        commentRepository.findById(reply.id) shouldBe null
        commentRepository.findAll() shouldHaveSize 1

        // When - Delete the root comment
        commentService.deleteComment(rootComment.id, userId1)

        // Then - Verify root comment was deleted
        commentRepository.findById(rootComment.id) shouldBe null
        commentRepository.findAll() shouldHaveSize 0
    }

    @Test
    fun `should handle comment with replies deletion correctly`() {
        // Given - Create a root comment
        val rootComment = commentService.createComment(validContent, userId1, postId)

        // Create multiple replies to the root comment
        val reply1 = commentService.createReply("Reply 1", userId2, postId, rootComment.id)
        val reply2 = commentService.createReply("Reply 2", userId1, postId, rootComment.id)
        val reply3 = commentService.createReply("Reply 3", userId2, postId, rootComment.id)

        // Verify initial state
        commentRepository.findAll() shouldHaveSize 4
        commentRepository.findRepliesByParentCommentId(rootComment.id) shouldHaveSize 3

        // When - Delete the root comment
        commentService.deleteComment(rootComment.id, userId1)

        // Then - Verify root comment and all replies were deleted
        commentRepository.findById(rootComment.id) shouldBe null
        commentRepository.findById(reply1.id) shouldBe null
        commentRepository.findById(reply2.id) shouldBe null
        commentRepository.findById(reply3.id) shouldBe null
        commentRepository.findAll() shouldHaveSize 0
    }

    @Test
    fun `should handle concurrent comment operations with thread safety`() {
        // Given
        val commentCount = 50
        val executor = Executors.newFixedThreadPool(10)

        try {
            // When - Multiple comments created concurrently
            val futures = (1..commentCount).map { i ->
                CompletableFuture.supplyAsync({
                    try {
                        commentService.createComment("Concurrent comment $i", userId1, postId)
                    } catch (e: Exception) {
                        null
                    }
                }, executor)
            }

            // Wait for all operations to complete
            val results = futures.map { it.get(5, TimeUnit.SECONDS) }

            // Then - Verify all comments were created
            val successfulComments = results.filterNotNull()
            successfulComments shouldHaveSize commentCount
            commentRepository.findAll() shouldHaveSize commentCount

            // When - Get comments for post
            val commentsWithReplies = commentService.getCommentsForPost(postId)

            // Then - Verify comments structure
            commentsWithReplies shouldHaveSize commentCount
            commentsWithReplies.forEach { it.replies shouldHaveSize 0 }

        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun `should handle editing a comment that has replies`() {
        // Given - Create a root comment
        val rootComment = commentService.createComment(validContent, userId1, postId)

        // Create replies to the root comment
        val reply1 = commentService.createReply("Reply 1", userId2, postId, rootComment.id)
        val reply2 = commentService.createReply("Reply 2", userId1, postId, rootComment.id)

        // Verify initial state
        commentRepository.findAll() shouldHaveSize 3
        commentRepository.findRepliesByParentCommentId(rootComment.id) shouldHaveSize 2

        // When - Edit the root comment
        val updatedContent = "Updated root comment with replies"
        val updatedComment = commentService.updateComment(rootComment.id, updatedContent, userId1)

        // Then - Verify root comment was updated but replies remain unchanged
        updatedComment.content shouldBe updatedContent
        commentRepository.findById(rootComment.id)?.content shouldBe updatedContent
        commentRepository.findById(reply1.id)?.content shouldBe "Reply 1"
        commentRepository.findById(reply2.id)?.content shouldBe "Reply 2"
        commentRepository.findAll() shouldHaveSize 3
    }

    @Test
    fun `should handle deleting a reply without affecting parent or sibling replies`() {
        // Given - Create a root comment
        val rootComment = commentService.createComment(validContent, userId1, postId)

        // Create multiple replies to the root comment
        val reply1 = commentService.createReply("Reply 1", userId2, postId, rootComment.id)
        val reply2 = commentService.createReply("Reply 2", userId1, postId, rootComment.id)
        val reply3 = commentService.createReply("Reply 3", userId2, postId, rootComment.id)

        // Verify initial state
        commentRepository.findAll() shouldHaveSize 4
        commentRepository.findRepliesByParentCommentId(rootComment.id) shouldHaveSize 3

        // When - Delete the second reply
        commentService.deleteComment(reply2.id, userId1)

        // Then - Verify only the second reply was deleted
        commentRepository.findById(rootComment.id) shouldNotBe null
        commentRepository.findById(reply1.id) shouldNotBe null
        commentRepository.findById(reply2.id) shouldBe null
        commentRepository.findById(reply3.id) shouldNotBe null
        commentRepository.findAll() shouldHaveSize 3
        commentRepository.findRepliesByParentCommentId(rootComment.id) shouldHaveSize 2
    }

    @Test
    fun `should handle error cases correctly`() {
        // Given - Create a root comment
        val rootComment = commentService.createComment(validContent, userId1, postId)

        // Test 1: Try to get non-existent comment
        val nonExistentCommentId = CommentId.generate()
        shouldThrow<CommentNotFoundException> {
            commentService.getComment(nonExistentCommentId)
        }

        // Test 2: Try to update non-existent comment
        shouldThrow<CommentNotFoundException> {
            commentService.updateComment(nonExistentCommentId, "Updated content", userId1)
        }

        // Test 3: Try to delete non-existent comment
        shouldThrow<CommentNotFoundException> {
            commentService.deleteComment(nonExistentCommentId, userId1)
        }

        // Test 4: Try to create reply to non-existent parent
        shouldThrow<CommentNotFoundException> {
            commentService.createReply("Reply to non-existent", userId1, postId, nonExistentCommentId)
        }

        // Test 5: Try to update comment with wrong user
        shouldThrow<UnauthorizedCommentModificationException> {
            commentService.updateComment(rootComment.id, "Unauthorized update", userId2)
        }

        // Test 6: Try to delete comment with wrong user
        shouldThrow<UnauthorizedCommentModificationException> {
            commentService.deleteComment(rootComment.id, userId2)
        }
    }

    @Test
    fun `should handle performance requirements for large datasets`() {
        // Given
        val commentCount = 100
        val replyCount = 5
        val rootComments = (1..commentCount).map { i ->
            commentService.createComment("Root comment $i", userId1, postId)
        }

        // Add replies to first 20 root comments
        rootComments.take(20).forEach { rootComment ->
            (1..replyCount).forEach { j ->
                commentService.createReply("Reply $j to comment ${rootComment.id}", userId2, postId, rootComment.id)
            }
        }

        // Verify initial state
        commentRepository.findAll() shouldHaveSize (commentCount + 20 * replyCount)

        // When - Measure performance of getting comments for post
        val startTime = System.currentTimeMillis()
        val commentsWithReplies = commentService.getCommentsForPost(postId)
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime

        // Then - Verify performance and correctness
        commentsWithReplies shouldHaveSize commentCount
        commentsWithReplies.count { it.replies.isNotEmpty() } shouldBe 20
        commentsWithReplies.sumOf { it.replies.size } shouldBe 20 * replyCount

        // Performance should be reasonable (less than 1 second for this operation)
        assert(executionTime < 1000) { "Operation took too long: ${executionTime}ms" }
    }
}

