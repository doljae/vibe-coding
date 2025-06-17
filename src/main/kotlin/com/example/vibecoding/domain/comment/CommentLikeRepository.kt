package com.example.vibecoding.domain.comment

import com.example.vibecoding.domain.user.UserId

/**
 * Repository interface for CommentLike domain
 */
interface CommentLikeRepository {
    /**
     * Save a comment like
     */
    fun save(commentLike: CommentLike): CommentLike

    /**
     * Find a comment like by its ID
     */
    fun findById(id: CommentLikeId): CommentLike?

    /**
     * Find all likes for a specific comment
     */
    fun findByCommentId(commentId: CommentId): List<CommentLike>

    /**
     * Find all likes by a specific user
     */
    fun findByUserId(userId: UserId): List<CommentLike>

    /**
     * Find a like by comment ID and user ID
     */
    fun findByCommentIdAndUserId(commentId: CommentId, userId: UserId): CommentLike?

    /**
     * Delete a comment like by its ID
     */
    fun deleteById(id: CommentLikeId): Boolean

    /**
     * Delete a comment like by comment ID and user ID
     */
    fun deleteByCommentIdAndUserId(commentId: CommentId, userId: UserId): Boolean

    /**
     * Delete all likes for a specific comment
     */
    fun deleteByCommentId(commentId: CommentId): Int

    /**
     * Count total likes for a specific comment
     */
    fun countByCommentId(commentId: CommentId): Long

    /**
     * Check if a user has liked a comment
     */
    fun existsByCommentIdAndUserId(commentId: CommentId, userId: UserId): Boolean
}

