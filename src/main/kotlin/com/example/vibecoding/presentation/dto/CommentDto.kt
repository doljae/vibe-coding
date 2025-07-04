package com.example.vibecoding.presentation.dto

import com.example.vibecoding.application.comment.CommentWithReplies
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Request DTO for creating a new comment
 */
data class CreateCommentRequest(
    @field:NotBlank(message = "Comment content cannot be blank")
    @field:Size(min = 1, max = 1000, message = "Comment content must be between 1 and 1000 characters")
    val content: String,
    
    @field:NotBlank(message = "Author name cannot be blank")
    val authorName: String,
    
    @field:NotBlank(message = "Post ID cannot be blank")
    val postId: String
) {
    fun toPostId(): PostId = PostId.from(postId)
}

/**
 * Request DTO for creating a reply to a comment
 */
data class CreateReplyRequest(
    @field:NotBlank(message = "Reply content cannot be blank")
    @field:Size(min = 1, max = 1000, message = "Reply content must be between 1 and 1000 characters")
    val content: String,
    
    @field:NotBlank(message = "Author name cannot be blank")
    val authorName: String,
    
    @field:NotBlank(message = "Post ID cannot be blank")
    val postId: String,
    
    @field:NotBlank(message = "Parent comment ID cannot be blank")
    val parentCommentId: String
) {
    fun toPostId(): PostId = PostId.from(postId)
    fun toParentCommentId(): CommentId = CommentId.from(parentCommentId)
}

/**
 * Request DTO for updating a comment
 */
data class UpdateCommentRequest(
    @field:NotBlank(message = "Comment content cannot be blank")
    @field:Size(min = 1, max = 1000, message = "Comment content must be between 1 and 1000 characters")
    val content: String,
    
    @field:NotBlank(message = "Author ID cannot be blank")
    val authorId: String
) {
    fun toUserId(): UserId = UserId.from(authorId)
}

/**
 * Response DTO for a comment
 */
data class CommentResponse(
    val id: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val postId: String,
    val parentCommentId: String?,
    val isReply: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(comment: Comment, userService: UserService? = null): CommentResponse {
            val authorName = try {
                userService?.getUserById(comment.authorId)?.displayName ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }
            
            return CommentResponse(
                id = comment.id.value.toString(),
                content = comment.content,
                authorId = comment.authorId.value.toString(),
                authorName = authorName,
                postId = comment.postId.value.toString(),
                parentCommentId = comment.parentCommentId?.value?.toString(),
                isReply = comment.isReply(),
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt
            )
        }
    }
}

/**
 * Response DTO for a comment with its replies
 */
data class CommentWithRepliesResponse(
    val comment: CommentResponse,
    val replies: List<CommentResponse>,
    val replyCount: Int
) {
    companion object {
        fun from(commentWithReplies: CommentWithReplies, userService: UserService? = null): CommentWithRepliesResponse {
            return CommentWithRepliesResponse(
                comment = CommentResponse.from(commentWithReplies.comment, userService),
                replies = commentWithReplies.replies.map { CommentResponse.from(it, userService) },
                replyCount = commentWithReplies.replies.size
            )
        }
    }
}

/**
 * Response DTO for post comments summary
 */
data class PostCommentsResponse(
    val postId: String,
    val comments: List<CommentWithRepliesResponse>,
    val totalCommentCount: Long
) {
    companion object {
        fun from(
            postId: PostId,
            commentsWithReplies: List<CommentWithReplies>,
            totalCount: Long,
            userService: UserService? = null
        ): PostCommentsResponse {
            return PostCommentsResponse(
                postId = postId.value.toString(),
                comments = commentsWithReplies.map { CommentWithRepliesResponse.from(it, userService) },
                totalCommentCount = totalCount
            )
        }
    }
}
