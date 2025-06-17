package com.example.vibecoding.comment

import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentRepository
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import com.example.vibecoding.infrastructure.repository.InMemoryCommentRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.UUID

/**
 * Functional tests for the comment functionality
 * Tests the complete flow of comment operations
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentFunctionalityTest {

    private lateinit var commentService: CommentService
    private lateinit var commentRepository: CommentRepository
    private lateinit var postRepository: PostRepository
    private lateinit var userRepository: UserRepository

    private val userId = UserId(UUID.randomUUID())
    private val postId = PostId(UUID.randomUUID())

    @BeforeEach
    fun setUp() {
        commentRepository = InMemoryCommentRepository()
        postRepository = mockk()
        userRepository = mockk()
        commentService = CommentService(commentRepository, postRepository, userRepository)

        // Mock post and user existence
        every { postRepository.findById(any()) } returns mockk(relaxed = true)
        every { userRepository.findById(any()) } returns mockk(relaxed = true)
    }

    @Test
    fun `should create and retrieve comments correctly`() {
        // Given
        val content = "Test comment content"

        // When - Create a comment
        val comment = commentService.createComment(content, userId, postId)

        // Then - Verify comment was created correctly
        comment.content shouldBe content
        comment.authorId shouldBe userId
        comment.postId shouldBe postId
        comment.parentCommentId shouldBe null
        comment.isRootComment() shouldBe true

        // When - Retrieve the comment
        val retrievedComment = commentService.getComment(comment.id)

        // Then - Verify retrieved comment matches created comment
        retrievedComment shouldBe comment
    }

    @Test
    fun `should create and retrieve replies correctly`() {
        // Given
        val rootComment = commentService.createComment("Root comment", userId, postId)
        val replyContent = "Reply to root comment"

        // When - Create a reply
        val reply = commentService.createReply(replyContent, userId, postId, rootComment.id)

        // Then - Verify reply was created correctly
        reply.content shouldBe replyContent
        reply.authorId shouldBe userId
        reply.postId shouldBe postId
        reply.parentCommentId shouldBe rootComment.id
        reply.isReply() shouldBe true

        // When - Get comments for post
        val commentsWithReplies = commentService.getCommentsForPost(postId)

        // Then - Verify comments structure
        commentsWithReplies.size shouldBe 1
        commentsWithReplies[0].comment shouldBe rootComment
        commentsWithReplies[0].replies.size shouldBe 1
        commentsWithReplies[0].replies[0] shouldBe reply
    }

    @Test
    fun `should update comments correctly`() {
        // Given
        val comment = commentService.createComment("Original content", userId, postId)
        val newContent = "Updated content"

        // When - Update the comment
        val updatedComment = commentService.updateComment(comment.id, newContent, userId)

        // Then - Verify comment was updated
        updatedComment.id shouldBe comment.id
        updatedComment.content shouldBe newContent
        updatedComment.authorId shouldBe userId
        updatedComment.postId shouldBe postId
        updatedComment.updatedAt shouldNotBe comment.updatedAt

        // When - Retrieve the comment
        val retrievedComment = commentService.getComment(comment.id)

        // Then - Verify retrieved comment has updated content
        retrievedComment.content shouldBe newContent
    }

    @Test
    fun `should delete comments and their replies correctly`() {
        // Given
        val rootComment = commentService.createComment("Root comment", userId, postId)
        val reply1 = commentService.createReply("Reply 1", userId, postId, rootComment.id)
        val reply2 = commentService.createReply("Reply 2", userId, postId, rootComment.id)

        // Verify initial state
        commentService.getCommentsForPost(postId)[0].replies.size shouldBe 2

        // When - Delete the root comment
        commentService.deleteComment(rootComment.id, userId)

        // Then - Verify root comment and all replies are deleted
        val remainingComments = commentService.getCommentsForPost(postId)
        remainingComments.size shouldBe 0

        // When - Create a new root comment and reply
        val newRootComment = commentService.createComment("New root comment", userId, postId)
        val newReply = commentService.createReply("New reply", userId, postId, newRootComment.id)

        // When - Delete just the reply
        commentService.deleteComment(newReply.id, userId)

        // Then - Verify only the reply is deleted
        val updatedComments = commentService.getCommentsForPost(postId)
        updatedComments.size shouldBe 1
        updatedComments[0].replies.size shouldBe 0
    }

    @Test
    fun `should handle multiple comments and replies correctly`() {
        // Given - Create multiple root comments
        val rootComment1 = commentService.createComment("Root comment 1", userId, postId)
        val rootComment2 = commentService.createComment("Root comment 2", userId, postId)
        val rootComment3 = commentService.createComment("Root comment 3", userId, postId)

        // Add replies to first root comment
        val reply1 = commentService.createReply("Reply 1 to comment 1", userId, postId, rootComment1.id)
        val reply2 = commentService.createReply("Reply 2 to comment 1", userId, postId, rootComment1.id)

        // Add reply to second root comment
        val reply3 = commentService.createReply("Reply to comment 2", userId, postId, rootComment2.id)

        // When - Get all comments for post
        val commentsWithReplies = commentService.getCommentsForPost(postId)

        // Then - Verify structure
        commentsWithReplies.size shouldBe 3

        // Find comment 1 and verify its replies
        val comment1WithReplies = commentsWithReplies.find { it.comment.id == rootComment1.id }
        comment1WithReplies shouldNotBe null
        comment1WithReplies!!.replies.size shouldBe 2

        // Find comment 2 and verify its reply
        val comment2WithReplies = commentsWithReplies.find { it.comment.id == rootComment2.id }
        comment2WithReplies shouldNotBe null
        comment2WithReplies!!.replies.size shouldBe 1

        // Find comment 3 and verify it has no replies
        val comment3WithReplies = commentsWithReplies.find { it.comment.id == rootComment3.id }
        comment3WithReplies shouldNotBe null
        comment3WithReplies!!.replies.size shouldBe 0

        // Verify total comment count
        commentService.getCommentCountForPost(postId) shouldBe 6
    }
}

