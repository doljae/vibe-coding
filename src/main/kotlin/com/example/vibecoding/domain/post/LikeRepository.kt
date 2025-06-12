package com.example.vibecoding.domain.post

import com.example.vibecoding.domain.user.UserId

/**
 * Repository interface for Like domain (Port in hexagonal architecture)
 */
interface LikeRepository {
    fun save(like: Like): Like
    fun findById(id: LikeId): Like?
    fun findByPostId(postId: PostId): List<Like>
    fun findByUserId(userId: UserId): List<Like>
    fun findByPostIdAndUserId(postId: PostId, userId: UserId): Like?
    fun delete(id: LikeId): Boolean
    fun deleteByPostIdAndUserId(postId: PostId, userId: UserId): Boolean
    fun existsByPostIdAndUserId(postId: PostId, userId: UserId): Boolean
    fun countByPostId(postId: PostId): Long
    fun countByUserId(userId: UserId): Long
}

