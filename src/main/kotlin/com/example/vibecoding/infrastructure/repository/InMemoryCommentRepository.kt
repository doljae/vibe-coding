package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentRepository
import com.example.vibecoding.domain.post.PostId
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of CommentRepository for testing purposes
 */
@Repository
class InMemoryCommentRepository : CommentRepository {
    private val comments = ConcurrentHashMap<CommentId, Comment>()

    override fun save(comment: Comment): Comment {
        comments[comment.id] = comment
        return comment
    }

    override fun findById(id: CommentId): Comment? {
        return comments[id]
    }

    override fun findByPostId(postId: PostId): List<Comment> {
        return comments.values.filter { it.postId == postId }
    }

    override fun findRootCommentsByPostId(postId: PostId): List<Comment> {
        return comments.values.filter { it.postId == postId && it.isRootComment() }
    }

    override fun findRepliesByParentCommentId(parentCommentId: CommentId): List<Comment> {
        return comments.values.filter { it.parentCommentId == parentCommentId }
    }

    override fun deleteById(id: CommentId): Boolean {
        return comments.remove(id) != null
    }

    override fun existsById(id: CommentId): Boolean {
        return comments.containsKey(id)
    }

    override fun countByPostId(postId: PostId): Long {
        return comments.values.count { it.postId == postId }.toLong()
    }

    override fun findAll(): List<Comment> {
        return comments.values.toList()
    }

    /**
     * Clear all comments (for testing purposes)
     */
    fun clear() {
        comments.clear()
    }
}

