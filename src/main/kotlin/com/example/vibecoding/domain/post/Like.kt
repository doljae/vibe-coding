package com.example.vibecoding.domain.post

import com.example.vibecoding.domain.user.UserId
import java.time.LocalDateTime
import java.util.*

/**
 * Like domain entity representing a user's like on a post
 */
data class Like(
    val id: LikeId,
    val postId: PostId,
    val userId: UserId,
    val createdAt: LocalDateTime
) {
    init {
        require(createdAt <= LocalDateTime.now()) { "Like creation date cannot be in the future" }
    }
}

/**
 * Value object for Like ID
 */
@JvmInline
value class LikeId(val value: UUID) {
    companion object {
        fun generate(): LikeId = LikeId(UUID.randomUUID())
        fun from(value: String): LikeId = LikeId(UUID.fromString(value))
    }
}

