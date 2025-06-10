package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Application service for Post domain operations
 */
@Service
class PostService(
    private val postRepository: PostRepository,
    private val categoryRepository: CategoryRepository
) {

    fun createPost(title: String, content: String, categoryId: CategoryId): Post {
        if (!categoryRepository.existsById(categoryId)) {
            throw CategoryNotFoundException("Category with id '$categoryId' not found")
        }

        val now = LocalDateTime.now()
        val post = Post(
            id = PostId.generate(),
            title = title,
            content = content,
            categoryId = categoryId,
            createdAt = now,
            updatedAt = now
        )

        return postRepository.save(post)
    }

    fun updatePost(id: PostId, title: String?, content: String?, categoryId: CategoryId?): Post {
        val existingPost = postRepository.findById(id)
            ?: throw PostNotFoundException("Post with id '$id' not found")

        var updatedPost = existingPost

        title?.let { newTitle ->
            updatedPost = updatedPost.updateTitle(newTitle)
        }

        content?.let { newContent ->
            updatedPost = updatedPost.updateContent(newContent)
        }

        categoryId?.let { newCategoryId ->
            if (!categoryRepository.existsById(newCategoryId)) {
                throw CategoryNotFoundException("Category with id '$newCategoryId' not found")
            }
            updatedPost = updatedPost.updateCategory(newCategoryId)
        }

        return postRepository.save(updatedPost)
    }

    fun getPostById(id: PostId): Post {
        return postRepository.findById(id)
            ?: throw PostNotFoundException("Post with id '$id' not found")
    }

    fun getAllPosts(): List<Post> {
        return postRepository.findAll()
    }

    fun getPostsByCategory(categoryId: CategoryId): List<Post> {
        if (!categoryRepository.existsById(categoryId)) {
            throw CategoryNotFoundException("Category with id '$categoryId' not found")
        }
        return postRepository.findByCategoryId(categoryId)
    }

    fun searchPostsByTitle(title: String): List<Post> {
        return postRepository.findByTitle(title)
    }

    fun deletePost(id: PostId) {
        if (!postRepository.existsById(id)) {
            throw PostNotFoundException("Post with id '$id' not found")
        }

        postRepository.delete(id)
    }
}

class PostNotFoundException(message: String) : RuntimeException(message)
class CategoryNotFoundException(message: String) : RuntimeException(message)

