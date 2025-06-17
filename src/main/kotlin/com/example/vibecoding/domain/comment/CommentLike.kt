package com.example.vibecoding.domain.comment

import com.example.vibecoding.domain.user.UserId
import java.time.LocalDateTime
import java.util.*

/**
 * CommentLike domain entity representing a like on a comment
 */
data class CommentLike(
    val id: CommentLikeId,
    val commentId: CommentId,
    val userId: UserId,
    val createdAt: LocalDateTime
)

/**
 * Value object for CommentLike ID
 */
@JvmInline
value class CommentLikeId(val value: UUID) {
    companion object {
        fun generate(): CommentLikeId = CommentLikeId(UUID.randomUUID())
        fun from(value: String): CommentLikeId = CommentLikeId(UUID.fromString(value))
    }
}

