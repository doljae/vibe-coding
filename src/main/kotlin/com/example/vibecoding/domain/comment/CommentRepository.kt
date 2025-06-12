package com.example.vibecoding.domain.comment

import com.example.vibecoding.domain.post.PostId

/**
 * Repository interface for Comment domain
 */
interface CommentRepository {
    /**
     * Save a comment
     */
    fun save(comment: Comment): Comment

    /**
     * Find a comment by its ID
     */
    fun findById(id: CommentId): Comment?

    /**
     * Find all comments for a specific post
     */
    fun findByPostId(postId: PostId): List<Comment>

    /**
     * Find all root comments for a specific post (comments without parent)
     */
    fun findRootCommentsByPostId(postId: PostId): List<Comment>

    /**
     * Find all replies to a specific comment
     */
    fun findRepliesByParentCommentId(parentCommentId: CommentId): List<Comment>

    /**
     * Delete a comment by its ID
     */
    fun deleteById(id: CommentId): Boolean

    /**
     * Check if a comment exists
     */
    fun existsById(id: CommentId): Boolean

    /**
     * Count total comments for a specific post
     */
    fun countByPostId(postId: PostId): Long

    /**
     * Find all comments (for testing purposes)
     */
    fun findAll(): List<Comment>
}

