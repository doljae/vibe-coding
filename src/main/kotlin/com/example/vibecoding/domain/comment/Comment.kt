package com.example.vibecoding.domain.comment

import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import java.time.LocalDateTime
import java.util.*

/**
 * Comment domain entity representing a comment on a post
 */
data class Comment(
    val id: CommentId,
    val content: String,
    val authorId: UserId,
    val postId: PostId,
    val parentCommentId: CommentId?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    init {
        require(content.isNotBlank()) { "Comment content cannot be blank" }
        require(content.length <= 1000) { "Comment content cannot exceed 1000 characters" }
    }

    /**
     * Check if this comment is a reply to another comment
     */
    fun isReply(): Boolean = parentCommentId != null

    /**
     * Check if this comment is a root comment (not a reply)
     */
    fun isRootComment(): Boolean = parentCommentId == null

    /**
     * Update the content of this comment
     */
    fun updateContent(newContent: String): Comment {
        require(newContent.isNotBlank()) { "Comment content cannot be blank" }
        require(newContent.length <= 1000) { "Comment content cannot exceed 1000 characters" }
        
        return copy(
            content = newContent,
            updatedAt = LocalDateTime.now()
        )
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

