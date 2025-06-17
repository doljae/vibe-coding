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
     * Find all comments for a post
     */
    fun findByPostId(postId: PostId): List<Comment>

    /**
     * Find all root comments (not replies) for a post
     */
    fun findRootCommentsByPostId(postId: PostId): List<Comment>

    /**
     * Find all replies to a parent comment
     */
    fun findRepliesByParentCommentId(parentCommentId: CommentId): List<Comment>

    /**
     * Delete a comment by its ID
     */
    fun deleteById(id: CommentId): Boolean

    /**
     * Delete all comments for a post
     */
    fun deleteByPostId(postId: PostId): Int

    /**
     * Check if a comment exists by its ID
     */
    fun existsById(id: CommentId): Boolean

    /**
     * Count comments for a post
     */
    fun countByPostId(postId: PostId): Long

    /**
     * Find all comments
     */
    fun findAll(): List<Comment>
}

