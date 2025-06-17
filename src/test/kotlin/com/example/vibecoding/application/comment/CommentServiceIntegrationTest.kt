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
import com.example.vibecoding.infrastructure.repository.InMemoryCommentRepository
import com.example.vibecoding.infrastructure.repository.InMemoryPostRepository
import com.example.vibecoding.infrastructure.repository.InMemoryUserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CommentServiceIntegrationTest {

    private lateinit var commentService: CommentServiceImpl
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
        commentRepository = InMemoryCommentRepository()
        postRepository = InMemoryPostRepository()
        userRepository = InMemoryUserRepository()
        commentService = CommentServiceImpl(commentRepository, postRepository, userRepository)

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

        userRepository.save(testUser)
        postRepository.save(testPost)
    }

    @Test
    fun `should create comment successfully`() {
        // When
        val result = commentService.createComment(validContent, userId, postId)

        // Then
        assertEquals(validContent, result.content)
        assertEquals(userId, result.authorId)
        assertEquals(postId, result.postId)
        assertNull(result.parentCommentId)
        assertTrue(result.isRootComment())
        
        // Verify the comment was saved
        val savedComment = commentRepository.findById(result.id)
        assertNotNull(savedComment)
    }

    @Test
    fun `should throw exception when creating comment for non-existent post`() {
        // Given
        val nonExistentPostId = PostId.generate()

        // When & Then
        assertThrows<PostNotFoundException> {
            commentService.createComment(validContent, userId, nonExistentPostId)
        }
    }

    @Test
    fun `should throw exception when creating comment for non-existent user`() {
        // Given
        val nonExistentUserId = UserId.generate()

        // When & Then
        assertThrows<UserNotFoundException> {
            commentService.createComment(validContent, nonExistentUserId, postId)
        }
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
        commentRepository.save(parentComment)

        // When
        val result = commentService.createReply(validContent, userId, postId, parentComment.id)

        // Then
        assertEquals(validContent, result.content)
        assertEquals(userId, result.authorId)
        assertEquals(postId, result.postId)
        assertEquals(parentComment.id, result.parentCommentId)
        assertTrue(result.isReply())
        
        // Verify the reply was saved
        val savedReply = commentRepository.findById(result.id)
        assertNotNull(savedReply)
    }

    @Test
    fun `should throw exception when creating reply to non-existent parent comment`() {
        // Given
        val nonExistentParentId = CommentId.generate()

        // When & Then
        assertThrows<CommentNotFoundException> {
            commentService.createReply(validContent, userId, postId, nonExistentParentId)
        }
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
        commentRepository.save(comment)

        // When
        val result = commentService.getComment(commentId)

        // Then
        assertEquals(comment, result)
    }

    @Test
    fun `should throw exception when getting non-existent comment`() {
        // When & Then
        assertThrows<CommentNotFoundException> {
            commentService.getComment(CommentId.generate())
        }
    }

    @Test
    fun `should get comment count for post successfully`() {
        // Given
        val comment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Comment 1",
            authorId = userId,
            postId = postId
        )
        val comment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Comment 2",
            authorId = userId,
            postId = postId
        )
        commentRepository.save(comment1)
        commentRepository.save(comment2)

        // When
        val result = commentService.getCommentCountForPost(postId)

        // Then
        assertEquals(2, result)
    }

    @Test
    fun `should check if comment exists successfully`() {
        // Given
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        commentRepository.save(comment)

        // When
        val result = commentService.commentExists(commentId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return false when comment does not exist`() {
        // When
        val result = commentService.commentExists(CommentId.generate())

        // Then
        assertFalse(result)
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
        
        commentRepository.save(rootComment1)
        commentRepository.save(rootComment2)
        commentRepository.save(reply1)

        // When
        val result = commentService.getCommentsForPost(postId)

        // Then
        assertEquals(2, result.size)
        
        val firstCommentWithReplies = result.find { it.comment.id == rootComment1.id }
        assertNotNull(firstCommentWithReplies)
        assertEquals(rootComment1, firstCommentWithReplies!!.comment)
        assertEquals(1, firstCommentWithReplies.replies.size)
        assertEquals(reply1, firstCommentWithReplies.replies[0])

        val secondCommentWithReplies = result.find { it.comment.id == rootComment2.id }
        assertNotNull(secondCommentWithReplies)
        assertEquals(rootComment2, secondCommentWithReplies!!.comment)
        assertEquals(0, secondCommentWithReplies.replies.size)
    }
}

