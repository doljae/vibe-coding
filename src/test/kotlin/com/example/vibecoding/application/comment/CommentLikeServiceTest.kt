package com.example.vibecoding.application.comment

import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentLike
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.infrastructure.repository.InMemoryCommentLikeRepository
import com.example.vibecoding.infrastructure.repository.InMemoryCommentRepository
import com.example.vibecoding.infrastructure.repository.InMemoryUserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*

class CommentLikeServiceTest {
    private lateinit var commentLikeRepository: InMemoryCommentLikeRepository
    private lateinit var commentRepository: InMemoryCommentRepository
    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var commentLikeService: CommentLikeService
    
    private lateinit var testUser: User
    private lateinit var testComment: Comment
    
    @BeforeEach
    fun setUp() {
        commentLikeRepository = InMemoryCommentLikeRepository()
        commentRepository = InMemoryCommentRepository()
        userRepository = InMemoryUserRepository()
        
        commentLikeService = CommentLikeServiceImpl(
            commentLikeRepository,
            commentRepository,
            userRepository
        )
        
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
        
        // Create test comment
        val commentId = CommentId.generate()
        testComment = Comment(
            id = commentId,
            content = "Test comment",
            authorId = userId,
            postId = PostId.generate(),
            parentCommentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        commentRepository.save(testComment)
    }
    
    @Test
    fun `should like a comment successfully`() {
        // When
        val commentLike = commentLikeService.likeComment(testComment.id, testUser.id)
        
        // Then
        assertNotNull(commentLike)
        assertEquals(testComment.id, commentLike.commentId)
        assertEquals(testUser.id, commentLike.userId)
        
        // Verify it was saved to repository
        val savedLike = commentLikeRepository.findById(commentLike.id)
        assertNotNull(savedLike)
    }
    
    @Test
    fun `should throw exception when liking a non-existent comment`() {
        // Given
        val nonExistentCommentId = CommentId.generate()
        
        // When & Then
        assertThrows<CommentNotFoundException> {
            commentLikeService.likeComment(nonExistentCommentId, testUser.id)
        }
    }
    
    @Test
    fun `should throw exception when liking with non-existent user`() {
        // Given
        val nonExistentUserId = UserId.generate()
        
        // When & Then
        assertThrows<UserNotFoundException> {
            commentLikeService.likeComment(testComment.id, nonExistentUserId)
        }
    }
    
    @Test
    fun `should throw exception when liking a comment that is already liked`() {
        // Given
        commentLikeService.likeComment(testComment.id, testUser.id)
        
        // When & Then
        assertThrows<CommentAlreadyLikedException> {
            commentLikeService.likeComment(testComment.id, testUser.id)
        }
    }
    
    @Test
    fun `should unlike a comment successfully`() {
        // Given
        commentLikeService.likeComment(testComment.id, testUser.id)
        
        // When
        val result = commentLikeService.unlikeComment(testComment.id, testUser.id)
        
        // Then
        assertTrue(result)
        
        // Verify it was removed from repository
        assertFalse(commentLikeRepository.existsByCommentIdAndUserId(testComment.id, testUser.id))
    }
    
    @Test
    fun `should return false when unliking a comment that is not liked`() {
        // When
        val result = commentLikeService.unlikeComment(testComment.id, testUser.id)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `should check if user has liked a comment`() {
        // Given
        commentLikeService.likeComment(testComment.id, testUser.id)
        
        // When
        val hasLiked = commentLikeService.hasUserLikedComment(testComment.id, testUser.id)
        
        // Then
        assertTrue(hasLiked)
    }
    
    @Test
    fun `should return false when checking if user has liked a comment they have not liked`() {
        // When
        val hasLiked = commentLikeService.hasUserLikedComment(testComment.id, testUser.id)
        
        // Then
        assertFalse(hasLiked)
    }
    
    @Test
    fun `should get like count for a comment`() {
        // Given
        commentLikeService.likeComment(testComment.id, testUser.id)
        
        // Create another user and like the comment
        val anotherUserId = UserId.generate()
        val anotherUser = User(
            id = anotherUserId,
            username = "anotheruser",
            email = "another@example.com",
            displayName = "Another User",
            bio = "Another bio",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(anotherUser)
        commentLikeService.likeComment(testComment.id, anotherUserId)
        
        // When
        val likeCount = commentLikeService.getLikeCount(testComment.id)
        
        // Then
        assertEquals(2, likeCount)
    }
    
    @Test
    fun `should get users who liked a comment`() {
        // Given
        commentLikeService.likeComment(testComment.id, testUser.id)
        
        // Create another user and like the comment
        val anotherUserId = UserId.generate()
        val anotherUser = User(
            id = anotherUserId,
            username = "anotheruser",
            email = "another@example.com",
            displayName = "Another User",
            bio = "Another bio",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(anotherUser)
        commentLikeService.likeComment(testComment.id, anotherUserId)
        
        // When
        val users = commentLikeService.getUsersWhoLikedComment(testComment.id)
        
        // Then
        assertEquals(2, users.size)
        assertTrue(users.contains(testUser.id))
        assertTrue(users.contains(anotherUserId))
    }
}

