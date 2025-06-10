package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Application service for Post domain operations
 */
@Service
class PostService(
    private val postRepository: PostRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository
) {

    fun createPost(title: String, content: String, authorId: UserId, categoryId: CategoryId): Post {
        if (userRepository.findById(authorId) == null) {
            throw UserNotFoundException("User with id '$authorId' not found")
        }
        
        if (!categoryRepository.existsById(categoryId)) {
            throw CategoryNotFoundException("Category with id '$categoryId' not found")
        }

        val now = LocalDateTime.now()
        val post = Post(
            id = PostId.generate(),
            title = title,
            content = content,
            authorId = authorId,
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

    fun getPostsByAuthor(authorId: UserId): List<Post> {
        if (userRepository.findById(authorId) == null) {
            throw UserNotFoundException("User with id '$authorId' not found")
        }
        return postRepository.findByAuthorId(authorId)
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

    fun getPostCountByCategory(categoryId: CategoryId): Long {
        return postRepository.countByCategoryId(categoryId)
    }

    fun getPostCountByAuthor(authorId: UserId): Long {
        return postRepository.countByAuthorId(authorId)
    }
}

class PostNotFoundException(message: String) : RuntimeException(message)
class CategoryNotFoundException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)
