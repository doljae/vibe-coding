package com.example.vibecoding.application.comment

import com.example.vibecoding.domain.comment.*
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Service interface for comment like operations
 */
interface CommentLikeService {
    /**
     * Like a comment
     */
    fun likeComment(commentId: CommentId, userId: UserId): CommentLike
    
    /**
     * Unlike a comment
     */
    fun unlikeComment(commentId: CommentId, userId: UserId): Boolean
    
    /**
     * Check if a user has liked a comment
     */
    fun hasUserLikedComment(commentId: CommentId, userId: UserId): Boolean
    
    /**
     * Get the number of likes for a comment
     */
    fun getLikeCount(commentId: CommentId): Long
    
    /**
     * Get all users who liked a comment
     */
    fun getUsersWhoLikedComment(commentId: CommentId): List<UserId>
}

/**
 * Implementation of the CommentLikeService interface
 */
@Service
class CommentLikeServiceImpl(
    private val commentLikeRepository: CommentLikeRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : CommentLikeService {
    
    override fun likeComment(commentId: CommentId, userId: UserId): CommentLike {
        // Validate that the comment exists
        val comment = commentRepository.findById(commentId) 
            ?: throw CommentNotFoundException("Comment not found")
        
        // Validate that the user exists
        val user = userRepository.findById(userId) 
            ?: throw UserNotFoundException("User not found")
        
        // Check if the user has already liked the comment
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw CommentAlreadyLikedException("User has already liked this comment")
        }
        
        // Create and save the like
        val commentLike = CommentLike(
            id = CommentLikeId.generate(),
            commentId = commentId,
            userId = userId,
            createdAt = LocalDateTime.now()
        )
        
        return commentLikeRepository.save(commentLike)
    }
    
    override fun unlikeComment(commentId: CommentId, userId: UserId): Boolean {
        // Validate that the comment exists
        val comment = commentRepository.findById(commentId) 
            ?: throw CommentNotFoundException("Comment not found")
        
        // Validate that the user exists
        val user = userRepository.findById(userId) 
            ?: throw UserNotFoundException("User not found")
        
        // Delete the like
        return commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId)
    }
    
    override fun hasUserLikedComment(commentId: CommentId, userId: UserId): Boolean {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)
    }
    
    override fun getLikeCount(commentId: CommentId): Long {
        return commentLikeRepository.countByCommentId(commentId)
    }
    
    override fun getUsersWhoLikedComment(commentId: CommentId): List<UserId> {
        return commentLikeRepository.findByCommentId(commentId).map { it.userId }
    }
}

// Exception class
class CommentAlreadyLikedException(message: String) : RuntimeException(message)

