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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*
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
        commentRepository = mock()
        postRepository = mock()
        userRepository = mock()
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
        whenever(postRepository.findById(postId)).thenReturn(testPost)
        whenever(userRepository.findById(userId)).thenReturn(testUser)
        whenever(commentRepository.save(any<Comment>())).thenAnswer { it.arguments[0] as Comment }

        // When
        val result = commentService.createComment(validContent, userId, postId)

        // Then
        assertEquals(validContent, result.content)
        assertEquals(userId, result.authorId)
        assertEquals(postId, result.postId)
        assertNull(result.parentCommentId)
        assertTrue(result.isRootComment())

        verify(postRepository).findById(postId)
        verify(userRepository).findById(userId)
        verify(commentRepository).save(any<Comment>())
    }

    @Test
    fun `should throw exception when creating comment for non-existent post`() {
        // Given
        whenever(postRepository.findById(postId)).thenReturn(null)

        // When & Then
        assertThrows<PostNotFoundException> {
            commentService.createComment(validContent, userId, postId)
        }

        verify(postRepository).findById(postId)
        verifyNoInteractions(userRepository)
        verifyNoInteractions(commentRepository)
    }

    @Test
    fun `should throw exception when creating comment for non-existent user`() {
        // Given
        whenever(postRepository.findById(postId)).thenReturn(testPost)
        whenever(userRepository.findById(userId)).thenReturn(null)

        // When & Then
        assertThrows<UserNotFoundException> {
            commentService.createComment(validContent, userId, postId)
        }

        verify(postRepository).findById(postId)
        verify(userRepository).findById(userId)
        verifyNoInteractions(commentRepository)
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

        whenever(commentRepository.findById(commentId)).thenReturn(comment)

        // When
        val result = commentService.getComment(commentId)

        // Then
        assertEquals(comment, result)
        verify(commentRepository).findById(commentId)
    }

    @Test
    fun `should throw exception when getting non-existent comment`() {
        // Given
        whenever(commentRepository.findById(commentId)).thenReturn(null)

        // When & Then
        assertThrows<CommentNotFoundException> {
            commentService.getComment(commentId)
        }

        verify(commentRepository).findById(commentId)
    }

    @Test
    fun `should get comment count for post`() {
        // Given
        val expectedCount = 5L
        whenever(commentRepository.countByPostId(postId)).thenReturn(expectedCount)

        // When
        val result = commentService.getCommentCountForPost(postId)

        // Then
        assertEquals(expectedCount, result)
        verify(commentRepository).countByPostId(postId)
    }

    @Test
    fun `should check if comment exists`() {
        // Given
        whenever(commentRepository.existsById(commentId)).thenReturn(true)

        // When
        val result = commentService.commentExists(commentId)

        // Then
        assertTrue(result)
        verify(commentRepository).existsById(commentId)
    }

    @Test
    fun `should return false when comment does not exist`() {
        // Given
        whenever(commentRepository.existsById(commentId)).thenReturn(false)

        // When
        val result = commentService.commentExists(commentId)

        // Then
        assertFalse(result)
        verify(commentRepository).existsById(commentId)
    }
}

