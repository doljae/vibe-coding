package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.post.LikeService
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.post.Like
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for Like operations
 */
@RestController
@RequestMapping("/api/likes")
class LikeController(
    private val likeService: LikeService,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(LikeController::class.java)

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
    @PutMapping("/posts/{postId}/users/{authorName}/toggle")
    fun toggleLike(
        @PathVariable postId: String,
        @PathVariable authorName: String
    ): ResponseEntity<LikeToggleResponse> {
        return try {
            logger.info("Toggling like for post: $postId, authorName: $authorName")
            
            // Find or create user by name
            val user = try {
                userService.getUserByUsername(authorName)
            } catch (e: Exception) {
                logger.info("User not found, creating new user: $authorName")
                userService.createUser(
                    username = authorName,
                    email = "${authorName.lowercase().replace(" ", "")}@example.com",
                    displayName = authorName
                )
            }
            
            val isLiked = likeService.toggleLike(
                PostId.from(postId),
                user.id
            )
            
            logger.info("Successfully toggled like for post: $postId, user: ${user.id}, isLiked: $isLiked")
            ResponseEntity.ok(LikeToggleResponse(isLiked))
            
        } catch (e: Exception) {
            logger.error("Failed to toggle like for post: $postId, authorName: $authorName", e)
            throw e
        }
    }

    /**
     * Check if user has liked a post
     */
    @GetMapping("/posts/{postId}/users/{authorName}/status")
    fun getLikeStatus(
        @PathVariable postId: String,
        @PathVariable authorName: String
    ): ResponseEntity<LikeStatusResponse> {
        return try {
            logger.info("Getting like status for post: $postId, authorName: $authorName")
            
            // Find user by name, return false if user doesn't exist
            val user = try {
                userService.getUserByUsername(authorName)
            } catch (e: Exception) {
                logger.info("User not found: $authorName, returning hasLiked: false")
                return ResponseEntity.ok(LikeStatusResponse(false))
            }
            
            val hasLiked = likeService.hasUserLikedPost(
                PostId.from(postId),
                user.id
            )
            
            logger.info("Like status for post: $postId, user: ${user.id}, hasLiked: $hasLiked")
            ResponseEntity.ok(LikeStatusResponse(hasLiked))
            
        } catch (e: Exception) {
            logger.error("Failed to get like status for post: $postId, authorName: $authorName", e)
            throw e
        }
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

