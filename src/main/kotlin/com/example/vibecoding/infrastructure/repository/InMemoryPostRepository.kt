package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of PostRepository (Adapter in hexagonal architecture)
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
        return posts.values.sortedByDescending { it.createdAt }
    }

    override fun findByCategoryId(categoryId: CategoryId): List<Post> {
        return posts.values
            .filter { it.categoryId == categoryId }
            .sortedByDescending { it.createdAt }
    }

    override fun findByTitle(title: String): List<Post> {
        return posts.values
            .filter { it.title.contains(title, ignoreCase = true) }
            .sortedByDescending { it.createdAt }
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
}

