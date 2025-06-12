package com.example.vibecoding.presentation.dto

import com.example.vibecoding.domain.post.ImageAttachment
import com.example.vibecoding.domain.post.Post
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Request DTO for creating a new post
 */
data class CreatePostRequest(
    @field:NotBlank(message = "Post title is required")
    @field:Size(max = 200, message = "Post title cannot exceed 200 characters")
    val title: String,
    
    @field:NotBlank(message = "Post content is required")
    @field:Size(max = 10000, message = "Post content cannot exceed 10000 characters")
    val content: String,
    
    @field:NotBlank(message = "Author ID is required")
    val authorId: String,
    
    @field:NotBlank(message = "Category ID is required")
    val categoryId: String
)

/**
 * Request DTO for updating an existing post
 */
data class UpdatePostRequest(
    @field:Size(max = 200, message = "Post title cannot exceed 200 characters")
    val title: String? = null,
    
    @field:Size(max = 10000, message = "Post content cannot exceed 10000 characters")
    val content: String? = null,
    
    val categoryId: String? = null
)

/**
 * Response DTO for post information
 */
data class PostResponse(
    val id: String,
    val title: String,
    val content: String,
    val author: UserSummaryResponse,
    val category: CategorySummaryResponse,
    val imageAttachments: List<ImageAttachmentResponse>,
    val likeCount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Summary response DTO for post lists
 */
data class PostSummaryResponse(
    val id: String,
    val title: String,
    val author: UserSummaryResponse,
    val category: CategorySummaryResponse,
    val imageCount: Int,
    val likeCount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Response DTO for image attachment information
 */
data class ImageAttachmentResponse(
    val id: String,
    val filename: String,
    val contentType: String,
    val fileSizeBytes: Long,
    val downloadUrl: String
) {
    companion object {
        fun from(imageAttachment: ImageAttachment, postId: String): ImageAttachmentResponse {
            return ImageAttachmentResponse(
                id = imageAttachment.id.value.toString(),
                filename = imageAttachment.filename,
                contentType = imageAttachment.contentType,
                fileSizeBytes = imageAttachment.fileSizeBytes,
                downloadUrl = "/api/posts/$postId/images/${imageAttachment.id.value}"
            )
        }
    }
}

/**
 * Request DTO for searching posts
 */
data class PostSearchRequest(
    val title: String? = null,
    val authorId: String? = null,
    val categoryId: String? = null
)
