package com.example.vibecoding.domain.comment

import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import java.time.LocalDateTime
import java.util.*

/**
 * Comment domain entity representing a comment on a blog post
 */
data class Comment(
    val id: CommentId,
    val content: String,
    val authorId: UserId,
    val postId: PostId,
    val parentCommentId: CommentId? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        const val MAX_CONTENT_LENGTH = 1000
        const val MIN_CONTENT_LENGTH = 1

        /**
         * Create a factory method for creating root comments
         */
        fun createRootComment(
            id: CommentId,
            content: String,
            authorId: UserId,
            postId: PostId,
            createdAt: LocalDateTime = LocalDateTime.now()
        ): Comment {
            return Comment(
                id = id,
                content = content,
                authorId = authorId,
                postId = postId,
                parentCommentId = null,
                createdAt = createdAt,
                updatedAt = createdAt
            )
        }

        /**
         * Create a factory method for creating reply comments
         */
        fun createReply(
            id: CommentId,
            content: String,
            authorId: UserId,
            postId: PostId,
            parentComment: Comment,
            createdAt: LocalDateTime = LocalDateTime.now()
        ): Comment {
            val reply = Comment(
                id = id,
                content = content,
                authorId = authorId,
                postId = postId,
                parentCommentId = parentComment.id,
                createdAt = createdAt,
                updatedAt = createdAt
            )
            
            reply.validateAsReplyTo(parentComment)
            return reply
        }
    }

    init {
        require(content.isNotBlank()) { "Comment content cannot be blank" }
        require(content.length >= MIN_CONTENT_LENGTH) { "Comment content must be at least $MIN_CONTENT_LENGTH character" }
        require(content.length <= MAX_CONTENT_LENGTH) { "Comment content cannot exceed $MAX_CONTENT_LENGTH characters" }
    }

    /**
     * Update the content of this comment
     */
    fun updateContent(newContent: String): Comment {
        require(newContent.isNotBlank()) { "Comment content cannot be blank" }
        require(newContent.length >= MIN_CONTENT_LENGTH) { "Comment content must be at least $MIN_CONTENT_LENGTH character" }
        require(newContent.length <= MAX_CONTENT_LENGTH) { "Comment content cannot exceed $MAX_CONTENT_LENGTH characters" }
        
        return copy(
            content = newContent,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Check if this comment is a reply to another comment
     */
    fun isReply(): Boolean {
        return parentCommentId != null
    }

    /**
     * Check if this comment is a root comment (not a reply)
     */
    fun isRootComment(): Boolean {
        return parentCommentId == null
    }

    /**
     * Validate that this comment can be a reply to the given parent comment
     * Business rule: Only 1-level deep replies are allowed
     */
    fun validateAsReplyTo(parentComment: Comment) {
        require(parentComment.isRootComment()) { 
            "Cannot reply to a comment that is already a reply. Only 1-level deep replies are allowed." 
        }
        require(parentComment.postId == this.postId) { 
            "Reply comment must belong to the same post as the parent comment" 
        }
    }
}

/**
 * Value object for Comment ID
 */
@JvmInline
value class CommentId(val value: UUID) {
    companion object {
        fun generate(): CommentId = CommentId(UUID.randomUUID())
        fun from(value: String): CommentId = CommentId(UUID.fromString(value))
    }
}

