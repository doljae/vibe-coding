package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.post.LikeService
import com.example.vibecoding.domain.post.Like
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for Like operations
 */
@RestController
@RequestMapping("/api/likes")
class LikeController(
    private val likeService: LikeService
) {

    /**
     * Like a post
     */
    @PostMapping("/posts/{postId}/users/{userId}")
    fun likePost(
        @PathVariable postId: String,
        @PathVariable userId: String
    ): ResponseEntity<LikeResponse> {
        val like = likeService.likePost(
            PostId.from(postId),
            UserId.from(userId)
        )
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(LikeResponse.from(like))
    }

    /**
     * Unlike a post
     */
    @DeleteMapping("/posts/{postId}/users/{userId}")
    fun unlikePost(
        @PathVariable postId: String,
        @PathVariable userId: String
    ): ResponseEntity<Void> {
        likeService.unlikePost(
            PostId.from(postId),
            UserId.from(userId)
        )
        
        return ResponseEntity.noContent().build()
    }

    /**
     * Toggle like status for a post
     */
    @PutMapping("/posts/{postId}/users/{userId}/toggle")
    fun toggleLike(
        @PathVariable postId: String,
        @PathVariable userId: String
    ): ResponseEntity<LikeToggleResponse> {
        val isLiked = likeService.toggleLike(
            PostId.from(postId),
            UserId.from(userId)
        )
        
        return ResponseEntity.ok(LikeToggleResponse(isLiked))
    }

    /**
     * Check if user has liked a post
     */
    @GetMapping("/posts/{postId}/users/{userId}/status")
    fun getLikeStatus(
        @PathVariable postId: String,
        @PathVariable userId: String
    ): ResponseEntity<LikeStatusResponse> {
        val hasLiked = likeService.hasUserLikedPost(
            PostId.from(postId),
            UserId.from(userId)
        )
        
        return ResponseEntity.ok(LikeStatusResponse(hasLiked))
    }

    /**
     * Get like count for a post
     */
    @GetMapping("/posts/{postId}/count")
    fun getLikeCount(
        @PathVariable postId: String
    ): ResponseEntity<LikeCountResponse> {
        val count = likeService.getLikeCountForPost(PostId.from(postId))
        return ResponseEntity.ok(LikeCountResponse(count))
    }

    /**
     * Get all likes for a post
     */
    @GetMapping("/posts/{postId}")
    fun getLikesForPost(
        @PathVariable postId: String
    ): ResponseEntity<List<LikeResponse>> {
        val likes = likeService.getLikesForPost(PostId.from(postId))
        val responses = likes.map { LikeResponse.from(it) }
        return ResponseEntity.ok(responses)
    }

    /**
     * Get all likes by a user
     */
    @GetMapping("/users/{userId}")
    fun getLikesByUser(
        @PathVariable userId: String
    ): ResponseEntity<List<LikeResponse>> {
        val likes = likeService.getLikesByUser(UserId.from(userId))
        val responses = likes.map { LikeResponse.from(it) }
        return ResponseEntity.ok(responses)
    }

    /**
     * Get like count by a user
     */
    @GetMapping("/users/{userId}/count")
    fun getLikeCountByUser(
        @PathVariable userId: String
    ): ResponseEntity<LikeCountResponse> {
        val count = likeService.getLikeCountByUser(UserId.from(userId))
        return ResponseEntity.ok(LikeCountResponse(count))
    }
}

/**
 * Response DTO for Like entity
 */
data class LikeResponse(
    val id: String,
    val postId: String,
    val userId: String,
    val createdAt: String
) {
    companion object {
        fun from(like: Like): LikeResponse {
            return LikeResponse(
                id = like.id.value.toString(),
                postId = like.postId.value.toString(),
                userId = like.userId.value.toString(),
                createdAt = like.createdAt.toString()
            )
        }
    }
}

/**
 * Response DTO for like toggle operation
 */
data class LikeToggleResponse(
    val isLiked: Boolean
)

/**
 * Response DTO for like status check
 */
data class LikeStatusResponse(
    val hasLiked: Boolean
)

/**
 * Response DTO for like count
 */
data class LikeCountResponse(
    val count: Long
)

