package com.example.vibecoding.application.comment

import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import com.example.vibecoding.domain.category.CategoryId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CommentServiceTest {

    private lateinit var commentService: CommentService
    private lateinit var commentRepository: CommentRepository
    private lateinit var postRepository: PostRepository
    private lateinit var userRepository: UserRepository

    private val userId = UserId.generate()
    private val postId = PostId.generate()
    private val commentId = CommentId.generate()
    private val validContent = "This is a valid comment"

    private lateinit var testUser: User
    private lateinit var testPost: Post

    @BeforeEach
    fun setUp() {
        commentRepository = mockk()
        postRepository = mockk()
        userRepository = mockk()
        commentService = CommentService(commentRepository, postRepository, userRepository)

        testUser = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        testPost = Post(
            id = postId,
            title = "Test Post",
            content = "Test post content",
            authorId = userId,
            categoryId = CategoryId.generate(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `should create comment successfully`() {
        // Given
        every { postRepository.findById(postId) } returns testPost
        every { userRepository.findById(userId) } returns testUser
        every { commentRepository.save(any<Comment>()) } answers { firstArg() }

        // When
        val result = commentService.createComment(validContent, userId, postId)

        // Then
        result.content shouldBe validContent
        result.authorId shouldBe userId
        result.postId shouldBe postId
        result.parentCommentId shouldBe null
        result.isRootComment() shouldBe true

        verify { postRepository.findById(postId) }
        verify { userRepository.findById(userId) }
        verify { commentRepository.save(any<Comment>()) }
    }

    @Test
    fun `should throw exception when creating comment for non-existent post`() {
        // Given
        every { postRepository.findById(postId) } returns null

        // When & Then
        shouldThrow<PostNotFoundException> {
            commentService.createComment(validContent, userId, postId)
        }

        verify { postRepository.findById(postId) }
    }

    @Test
    fun `should throw exception when creating comment for non-existent user`() {
        // Given
        every { postRepository.findById(postId) } returns testPost
        every { userRepository.findById(userId) } returns null

        // When & Then
        shouldThrow<UserNotFoundException> {
            commentService.createComment(validContent, userId, postId)
        }

        verify { postRepository.findById(postId) }
        verify { userRepository.findById(userId) }
    }

    @Test
    fun `should create reply successfully`() {
        // Given
        val parentComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Parent comment",
            authorId = userId,
            postId = postId
        )

        every { postRepository.findById(postId) } returns testPost
        every { userRepository.findById(userId) } returns testUser
        every { commentRepository.findById(parentComment.id) } returns parentComment
        every { commentRepository.save(any<Comment>()) } answers { firstArg() }

        // When
        val result = commentService.createReply(validContent, userId, postId, parentComment.id)

        // Then
        result.content shouldBe validContent
        result.authorId shouldBe userId
        result.postId shouldBe postId
        result.parentCommentId shouldBe parentComment.id
        result.isReply() shouldBe true

        verify { postRepository.findById(postId) }
        verify { userRepository.findById(userId) }
        verify { commentRepository.findById(parentComment.id) }
        verify { commentRepository.save(any<Comment>()) }
    }

    @Test
    fun `should throw exception when creating reply to non-existent parent comment`() {
        // Given
        val nonExistentParentId = CommentId.generate()
        every { postRepository.findById(postId) } returns testPost
        every { userRepository.findById(userId) } returns testUser
        every { commentRepository.findById(nonExistentParentId) } returns null

        // When & Then
        shouldThrow<CommentNotFoundException> {
            commentService.createReply(validContent, userId, postId, nonExistentParentId)
        }

        verify { commentRepository.findById(nonExistentParentId) }
    }

    @Test
    fun `should get comment successfully`() {
        // Given
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )

        every { commentRepository.findById(commentId) } returns comment

        // When
        val result = commentService.getComment(commentId)

        // Then
        result shouldBe comment
        verify { commentRepository.findById(commentId) }
    }

    @Test
    fun `should throw exception when getting non-existent comment`() {
        // Given
        every { commentRepository.findById(commentId) } returns null

        // When & Then
        shouldThrow<CommentNotFoundException> {
            commentService.getComment(commentId)
        }

        verify { commentRepository.findById(commentId) }
    }

    @Test
    fun `should get comment count for post`() {
        // Given
        val expectedCount = 5L
        every { commentRepository.countByPostId(postId) } returns expectedCount

        // When
        val result = commentService.getCommentCountForPost(postId)

        // Then
        result shouldBe expectedCount
        verify { commentRepository.countByPostId(postId) }
    }

    @Test
    fun `should check if comment exists`() {
        // Given
        every { commentRepository.existsById(commentId) } returns true

        // When
        val result = commentService.commentExists(commentId)

        // Then
        result shouldBe true
        verify { commentRepository.existsById(commentId) }
    }

    @Test
    fun `should return false when comment does not exist`() {
        // Given
        every { commentRepository.existsById(commentId) } returns false

        // When
        val result = commentService.commentExists(commentId)

        // Then
        result shouldBe false
        verify { commentRepository.existsById(commentId) }
    }

    @Test
    fun `should get comments for post with replies`() {
        // Given
        val rootComment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Root comment 1",
            authorId = userId,
            postId = postId
        )
        val rootComment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Root comment 2",
            authorId = userId,
            postId = postId
        )
        val reply1 = Comment.createReply(
            id = CommentId.generate(),
            content = "Reply to comment 1",
            authorId = userId,
            postId = postId,
            parentComment = rootComment1
        )

        every { postRepository.findById(postId) } returns testPost
        every { commentRepository.findRootCommentsByPostId(postId) } returns listOf(rootComment1, rootComment2)
        every { commentRepository.findRepliesByParentCommentId(rootComment1.id) } returns listOf(reply1)
        every { commentRepository.findRepliesByParentCommentId(rootComment2.id) } returns emptyList()

        // When
        val result = commentService.getCommentsForPost(postId)

        // Then
        result.size shouldBe 2
        
        val firstCommentWithReplies = result.find { it.comment.id == rootComment1.id }
        firstCommentWithReplies shouldNotBe null
        firstCommentWithReplies!!.comment shouldBe rootComment1
        firstCommentWithReplies.replies.size shouldBe 1
        firstCommentWithReplies.replies[0] shouldBe reply1

        val secondCommentWithReplies = result.find { it.comment.id == rootComment2.id }
        secondCommentWithReplies shouldNotBe null
        secondCommentWithReplies!!.comment shouldBe rootComment2
        secondCommentWithReplies.replies.size shouldBe 0

        verify { postRepository.findById(postId) }
        verify { commentRepository.findRootCommentsByPostId(postId) }
        verify { commentRepository.findRepliesByParentCommentId(rootComment1.id) }
        verify { commentRepository.findRepliesByParentCommentId(rootComment2.id) }
    }
}

