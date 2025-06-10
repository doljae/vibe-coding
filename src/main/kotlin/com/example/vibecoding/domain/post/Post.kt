package com.example.vibecoding.domain.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.user.UserId
import java.time.LocalDateTime
import java.util.*

/**
 * Post domain entity representing a blog post
 */
data class Post(
    val id: PostId,
    val title: String,
    val content: String,
    val authorId: UserId,
    val categoryId: CategoryId,
    val imageAttachments: List<ImageAttachment> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        const val MAX_IMAGES_PER_POST = 3
    }

    init {
        require(title.isNotBlank()) { "Post title cannot be blank" }
        require(title.length <= 200) { "Post title cannot exceed 200 characters" }
        require(content.isNotBlank()) { "Post content cannot be blank" }
        require(content.length <= 10000) { "Post content cannot exceed 10000 characters" }
        require(imageAttachments.size <= MAX_IMAGES_PER_POST) { "Post cannot have more than $MAX_IMAGES_PER_POST images" }
        require(imageAttachments.distinctBy { it.id }.size == imageAttachments.size) { "Post cannot have duplicate image attachments" }
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

    /**
     * Add an image attachment to the post
     */
    fun addImageAttachment(imageAttachment: ImageAttachment): Post {
        require(imageAttachments.size < MAX_IMAGES_PER_POST) { 
            "Cannot add image: Post already has maximum of $MAX_IMAGES_PER_POST images" 
        }
        require(!imageAttachments.any { it.id == imageAttachment.id }) { 
            "Image attachment with id '${imageAttachment.id}' already exists in this post" 
        }
        
        return copy(
            imageAttachments = imageAttachments + imageAttachment,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Remove an image attachment from the post
     */
    fun removeImageAttachment(imageId: ImageId): Post {
        val updatedAttachments = imageAttachments.filterNot { it.id == imageId }
        require(updatedAttachments.size < imageAttachments.size) { 
            "Image attachment with id '$imageId' not found in this post" 
        }
        
        return copy(
            imageAttachments = updatedAttachments,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Get an image attachment by ID
     */
    fun getImageAttachment(imageId: ImageId): ImageAttachment? {
        return imageAttachments.find { it.id == imageId }
    }

    /**
     * Check if the post has any image attachments
     */
    fun hasImageAttachments(): Boolean {
        return imageAttachments.isNotEmpty()
    }

    /**
     * Get the number of image attachments
     */
    fun getImageAttachmentCount(): Int {
        return imageAttachments.size
    }

    /**
     * Check if the post can accept more image attachments
     */
    fun canAddMoreImages(): Boolean {
        return imageAttachments.size < MAX_IMAGES_PER_POST
    }

    /**
     * Get the remaining image slots available
     */
    fun getRemainingImageSlots(): Int {
        return MAX_IMAGES_PER_POST - imageAttachments.size
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
