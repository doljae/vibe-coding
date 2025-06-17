package com.example.vibecoding.application.comment

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.infrastructure.repository.InMemoryCommentRepository
import com.example.vibecoding.infrastructure.repository.InMemoryPostRepository
import com.example.vibecoding.infrastructure.repository.InMemoryUserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*

class InMemoryCommentServiceTest {
    private lateinit var commentRepository: InMemoryCommentRepository
    private lateinit var postRepository: InMemoryPostRepository
    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var commentService: CommentService
    
    private lateinit var testUser: User
    private lateinit var testPost: Post
    private lateinit var testComment: Comment
    
    @BeforeEach
    fun setUp() {
        commentRepository = InMemoryCommentRepository()
        postRepository = InMemoryPostRepository()
        userRepository = InMemoryUserRepository()
        
        commentService = CommentServiceImpl(commentRepository, postRepository, userRepository)
        
        // Create test user
        val userId = UserId.generate()
        testUser = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = "Test bio",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(testUser)
        
        // Create test post
        val postId = PostId.generate()
        val categoryId = CategoryId(UUID.randomUUID())
        testPost = Post(
            id = postId,
            title = "Test Post",
            content = "Test content",
            authorId = userId,
            categoryId = categoryId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        postRepository.save(testPost)
        
        // Create test comment
        val commentId = CommentId.generate()
        testComment = Comment(
            id = commentId,
            content = "Test comment",
            authorId = userId,
            postId = postId,
            parentCommentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        commentRepository.save(testComment)
    }
    
    @Test
    fun `should create a comment successfully`() {
        // Given
        val content = "New comment"
        
        // When
        val createdComment = commentService.createComment(content, testUser.id, testPost.id)
        
        // Then
        assertNotNull(createdComment)
        assertEquals(content, createdComment.content)
        assertEquals(testUser.id, createdComment.authorId)
        assertEquals(testPost.id, createdComment.postId)
        assertNull(createdComment.parentCommentId)
        
        // Verify it was saved to repository
        val savedComment = commentRepository.findById(createdComment.id)
        assertNotNull(savedComment)
    }
    
    @Test
    fun `should create a reply successfully`() {
        // Given
        val replyContent = "Reply to comment"
        
        // When
        val reply = commentService.createReply(replyContent, testUser.id, testPost.id, testComment.id)
        
        // Then
        assertNotNull(reply)
        assertEquals(replyContent, reply.content)
        assertEquals(testUser.id, reply.authorId)
        assertEquals(testPost.id, reply.postId)
        assertEquals(testComment.id, reply.parentCommentId)
        
        // Verify it was saved to repository
        val savedReply = commentRepository.findById(reply.id)
        assertNotNull(savedReply)
    }
    
    @Test
    fun `should throw exception when creating reply to non-existent comment`() {
        // Given
        val nonExistentCommentId = CommentId.generate()
        val replyContent = "Reply to non-existent comment"
        
        // When & Then
        assertThrows<CommentNotFoundException> {
            commentService.createReply(replyContent, testUser.id, testPost.id, nonExistentCommentId)
        }
    }
    
    @Test
    fun `should throw exception when creating reply to a reply`() {
        // Given
        val firstReply = commentService.createReply("First reply", testUser.id, testPost.id, testComment.id)
        
        // When & Then
        assertThrows<InvalidCommentReplyException> {
            commentService.createReply("Reply to a reply", testUser.id, testPost.id, firstReply.id)
        }
    }
    
    @Test
    fun `should get comment by id`() {
        // When
        val retrievedComment = commentService.getComment(testComment.id)
        
        // Then
        assertNotNull(retrievedComment)
        assertEquals(testComment.id, retrievedComment.id)
        assertEquals(testComment.content, retrievedComment.content)
    }
    
    @Test
    fun `should throw exception when getting non-existent comment`() {
        // Given
        val nonExistentCommentId = CommentId.generate()
        
        // When & Then
        assertThrows<CommentNotFoundException> {
            commentService.getComment(nonExistentCommentId)
        }
    }
    
    @Test
    fun `should get comments for post with replies`() {
        // Given
        val reply1 = commentService.createReply("Reply 1", testUser.id, testPost.id, testComment.id)
        val reply2 = commentService.createReply("Reply 2", testUser.id, testPost.id, testComment.id)
        val anotherComment = commentService.createComment("Another comment", testUser.id, testPost.id)
        
        // When
        val commentsWithReplies = commentService.getCommentsForPost(testPost.id)
        
        // Then
        assertEquals(2, commentsWithReplies.size)
        
        // Find the test comment with its replies
        val testCommentWithReplies = commentsWithReplies.find { it.comment.id == testComment.id }
        assertNotNull(testCommentWithReplies)
        assertEquals(2, testCommentWithReplies!!.replies.size)
        
        // Find the other comment with no replies
        val otherCommentWithReplies = commentsWithReplies.find { it.comment.id == anotherComment.id }
        assertNotNull(otherCommentWithReplies)
        assertEquals(0, otherCommentWithReplies!!.replies.size)
    }
    
    @Test
    fun `should update comment successfully`() {
        // Given
        val newContent = "Updated content"
        
        // When
        val updatedComment = commentService.updateComment(testComment.id, newContent, testUser.id)
        
        // Then
        assertEquals(newContent, updatedComment.content)
        assertTrue(updatedComment.updatedAt.isAfter(testComment.updatedAt))
        
        // Verify it was updated in repository
        val savedComment = commentRepository.findById(testComment.id)
        assertEquals(newContent, savedComment?.content)
    }
    
    @Test
    fun `should throw exception when updating non-existent comment`() {
        // Given
        val nonExistentCommentId = CommentId.generate()
        val newContent = "Updated content"
        
        // When & Then
        assertThrows<CommentNotFoundException> {
            commentService.updateComment(nonExistentCommentId, newContent, testUser.id)
        }
    }
    
    @Test
    fun `should throw exception when unauthorized user tries to update comment`() {
        // Given
        val anotherUserId = UserId.generate()
        val newContent = "Updated by another user"
        
        // When & Then
        assertThrows<UnauthorizedCommentModificationException> {
            commentService.updateComment(testComment.id, newContent, anotherUserId)
        }
    }
    
    @Test
    fun `should delete comment successfully`() {
        // When
        val result = commentService.deleteComment(testComment.id, testUser.id)
        
        // Then
        assertTrue(result)
        
        // Verify it was deleted from repository
        assertThrows<CommentNotFoundException> {
            commentService.getComment(testComment.id)
        }
    }
    
    @Test
    fun `should throw exception when deleting non-existent comment`() {
        // Given
        val nonExistentCommentId = CommentId.generate()
        
        // When & Then
        assertThrows<CommentNotFoundException> {
            commentService.deleteComment(nonExistentCommentId, testUser.id)
        }
    }
    
    @Test
    fun `should throw exception when unauthorized user tries to delete comment`() {
        // Given
        val anotherUserId = UserId.generate()
        
        // When & Then
        assertThrows<UnauthorizedCommentModificationException> {
            commentService.deleteComment(testComment.id, anotherUserId)
        }
    }
    
    @Test
    fun `should delete comment and all its replies`() {
        // Given
        val reply1 = commentService.createReply("Reply 1", testUser.id, testPost.id, testComment.id)
        val reply2 = commentService.createReply("Reply 2", testUser.id, testPost.id, testComment.id)
        
        // When
        val result = commentService.deleteComment(testComment.id, testUser.id)
        
        // Then
        assertTrue(result)
        
        // Verify comment and replies were deleted
        assertThrows<CommentNotFoundException> {
            commentService.getComment(testComment.id)
        }
        
        assertThrows<CommentNotFoundException> {
            commentService.getComment(reply1.id)
        }
        
        assertThrows<CommentNotFoundException> {
            commentService.getComment(reply2.id)
        }
    }
}

