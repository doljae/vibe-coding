package com.example.vibecoding.domain.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.user.UserId

/**
 * Repository interface for Post domain (Port in hexagonal architecture)
 */
interface PostRepository {
    fun save(post: Post): Post
    fun findById(id: PostId): Post?
    fun findAll(): List<Post>
    fun findByCategoryId(categoryId: CategoryId): List<Post>
    fun findByAuthorId(authorId: UserId): List<Post>
    fun findByTitle(title: String): List<Post>
    fun delete(id: PostId): Boolean
    fun existsById(id: PostId): Boolean
    fun countByCategoryId(categoryId: CategoryId): Long
    fun countByAuthorId(authorId: UserId): Long
}

