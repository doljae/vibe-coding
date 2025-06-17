package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentLike
import com.example.vibecoding.domain.comment.CommentLikeId
import com.example.vibecoding.domain.comment.CommentLikeRepository
import com.example.vibecoding.domain.user.UserId
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of CommentLikeRepository for testing and development
 */
@Repository
class InMemoryCommentLikeRepository : CommentLikeRepository {
    private val commentLikes = ConcurrentHashMap<CommentLikeId, CommentLike>()

    override fun save(commentLike: CommentLike): CommentLike {
        commentLikes[commentLike.id] = commentLike
        return commentLike
    }

    override fun findById(id: CommentLikeId): CommentLike? {
        return commentLikes[id]
    }

    override fun findByCommentId(commentId: CommentId): List<CommentLike> {
        return commentLikes.values.filter { it.commentId == commentId }
    }

    override fun findByUserId(userId: UserId): List<CommentLike> {
        return commentLikes.values.filter { it.userId == userId }
    }

    override fun findByCommentIdAndUserId(commentId: CommentId, userId: UserId): CommentLike? {
        return commentLikes.values.find { it.commentId == commentId && it.userId == userId }
    }

    override fun deleteById(id: CommentLikeId): Boolean {
        return commentLikes.remove(id) != null
    }

    override fun deleteByCommentIdAndUserId(commentId: CommentId, userId: UserId): Boolean {
        val like = findByCommentIdAndUserId(commentId, userId)
        return like?.let { deleteById(it.id) } ?: false
    }

    override fun deleteByCommentId(commentId: CommentId): Int {
        val likesToDelete = commentLikes.values.filter { it.commentId == commentId }
        likesToDelete.forEach { commentLikes.remove(it.id) }
        return likesToDelete.size
    }

    override fun countByCommentId(commentId: CommentId): Long {
        return commentLikes.values.count { it.commentId == commentId }.toLong()
    }

    override fun existsByCommentIdAndUserId(commentId: CommentId, userId: UserId): Boolean {
        return commentLikes.values.any { it.commentId == commentId && it.userId == userId }
    }

    /**
     * Clear all comment likes (for testing)
     */
    fun clear() {
        commentLikes.clear()
    }
}

