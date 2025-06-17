package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.UserId
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of PostRepository for testing purposes
 */
@Repository
class InMemoryPostRepository : PostRepository {
    private val posts = ConcurrentHashMap<PostId, Post>()

    override fun save(post: Post): Post {
        posts[post.id] = post
        return post
    }

    override fun findById(id: PostId): Post? {
        return posts[id]
    }

    override fun findAll(): List<Post> {
        return posts.values.toList()
    }

    override fun findByCategoryId(categoryId: CategoryId): List<Post> {
        return posts.values.filter { it.categoryId == categoryId }
    }

    override fun findByAuthorId(authorId: UserId): List<Post> {
        return posts.values.filter { it.authorId == authorId }
    }

    override fun findByTitle(title: String): List<Post> {
        return posts.values.filter { it.title.contains(title, ignoreCase = true) }
    }

    override fun delete(id: PostId): Boolean {
        return posts.remove(id) != null
    }

    override fun existsById(id: PostId): Boolean {
        return posts.containsKey(id)
    }

    override fun countByCategoryId(categoryId: CategoryId): Long {
        return posts.values.count { it.categoryId == categoryId }.toLong()
    }

    override fun countByAuthorId(authorId: UserId): Long {
        return posts.values.count { it.authorId == authorId }.toLong()
    }

    /**
     * Clear all posts (for testing purposes)
     */
    fun clear() {
        posts.clear()
    }
}

