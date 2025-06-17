package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.post.Like
import com.example.vibecoding.domain.post.LikeId
import com.example.vibecoding.domain.post.LikeRepository
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of LikeRepository (Adapter in hexagonal architecture)
 * This implementation is thread-safe using ConcurrentHashMap
 */
@Repository
class InMemoryLikeRepository : LikeRepository {
    
    private val likes = ConcurrentHashMap<LikeId, Like>()

    override fun save(like: Like): Like {
        likes[like.id] = like
        return like
    }

    override fun findById(id: LikeId): Like? {
        return likes[id]
    }

    override fun findByPostId(postId: PostId): List<Like> {
        return likes.values
            .filter { it.postId == postId }
            .sortedByDescending { it.createdAt }
    }

    override fun findByUserId(userId: UserId): List<Like> {
        return likes.values
            .filter { it.userId == userId }
            .sortedByDescending { it.createdAt }
    }

    override fun findByPostIdAndUserId(postId: PostId, userId: UserId): Like? {
        return likes.values
            .find { it.postId == postId && it.userId == userId }
    }

    override fun delete(id: LikeId): Boolean {
        return likes.remove(id) != null
    }

    override fun deleteByPostIdAndUserId(postId: PostId, userId: UserId): Boolean {
        val like = findByPostIdAndUserId(postId, userId)
        return if (like != null) {
            likes.remove(like.id) != null
        } else {
            false
        }
    }

    override fun deleteByPostId(postId: PostId): Int {
        val likesToDelete = likes.values.filter { it.postId == postId }
        var count = 0
        
        likesToDelete.forEach { like ->
            if (likes.remove(like.id) != null) {
                count++
            }
        }
        
        return count
    }

    override fun existsByPostIdAndUserId(postId: PostId, userId: UserId): Boolean {
        return likes.values.any { it.postId == postId && it.userId == userId }
    }

    override fun countByPostId(postId: PostId): Long {
        return likes.values.count { it.postId == postId }.toLong()
    }

    override fun countByUserId(userId: UserId): Long {
        return likes.values.count { it.userId == userId }.toLong()
    }

    // Test helper methods
    fun clear() {
        likes.clear()
    }

    fun size(): Int {
        return likes.size
    }
}
