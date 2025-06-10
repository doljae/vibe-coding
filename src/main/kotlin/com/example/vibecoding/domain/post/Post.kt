package com.example.vibecoding.domain.post

import com.example.vibecoding.domain.category.CategoryId
import java.time.LocalDateTime
import java.util.*

/**
 * Post domain entity representing a blog post
 */
data class Post(
    val id: PostId,
    val title: String,
    val content: String,
    val categoryId: CategoryId,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    init {
        require(title.isNotBlank()) { "Post title cannot be blank" }
        require(title.length <= 200) { "Post title cannot exceed 200 characters" }
        require(content.isNotBlank()) { "Post content cannot be blank" }
        require(content.length <= 10000) { "Post content cannot exceed 10000 characters" }
    }

    fun updateTitle(newTitle: String): Post {
        require(newTitle.isNotBlank()) { "Post title cannot be blank" }
        require(newTitle.length <= 200) { "Post title cannot exceed 200 characters" }
        
        return copy(
            title = newTitle,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateContent(newContent: String): Post {
        require(newContent.isNotBlank()) { "Post content cannot be blank" }
        require(newContent.length <= 10000) { "Post content cannot exceed 10000 characters" }
        
        return copy(
            content = newContent,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateCategory(newCategoryId: CategoryId): Post {
        return copy(
            categoryId = newCategoryId,
            updatedAt = LocalDateTime.now()
        )
    }
}

/**
 * Value object for Post ID
 */
@JvmInline
value class PostId(val value: UUID) {
    companion object {
        fun generate(): PostId = PostId(UUID.randomUUID())
        fun from(value: String): PostId = PostId(UUID.fromString(value))
    }
}

