package com.example.vibecoding.application.post

import com.example.vibecoding.domain.post.*
import com.example.vibecoding.domain.user.UserId
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Application service for Like domain operations
 */
@Service
class LikeService(
    private val likeRepository: LikeRepository,
    private val postRepository: PostRepository
) {

    /**
     * Like a post by a user
     * @param postId The ID of the post to like
     * @param userId The ID of the user who likes the post
     * @return The created Like entity
     * @throws PostNotFoundException if the post doesn't exist
     * @throws DuplicateLikeException if the user has already liked the post
     */
    fun likePost(postId: PostId, userId: UserId): Like {
        // Check if post exists
        val post = postRepository.findById(postId)
            ?: throw PostNotFoundException("Post with id '$postId' not found")

        // Check if user has already liked this post
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw DuplicateLikeException("User '$userId' has already liked post '$postId'")
        }

        // Create and save the like
        val like = Like(
            id = LikeId.generate(),
            postId = postId,
            userId = userId,
            createdAt = LocalDateTime.now()
        )

        val savedLike = likeRepository.save(like)

        // Update post like count
        val updatedPost = post.incrementLikeCount()
        postRepository.save(updatedPost)

        return savedLike
    }

    /**
     * Unlike a post by a user
     * @param postId The ID of the post to unlike
     * @param userId The ID of the user who unlikes the post
     * @throws PostNotFoundException if the post doesn't exist
     * @throws LikeNotFoundException if the user hasn't liked the post
     */
    fun unlikePost(postId: PostId, userId: UserId) {
        // Check if post exists
        val post = postRepository.findById(postId)
            ?: throw PostNotFoundException("Post with id '$postId' not found")

        // Check if user has liked this post
        val existingLike = likeRepository.findByPostIdAndUserId(postId, userId)
            ?: throw LikeNotFoundException("User '$userId' has not liked post '$postId'")

        // Remove the like
        likeRepository.delete(existingLike.id)

        // Update post like count
        val updatedPost = post.decrementLikeCount()
        postRepository.save(updatedPost)
    }

    /**
     * Toggle like status for a post by a user
     * @param postId The ID of the post
     * @param userId The ID of the user
     * @return true if the post was liked, false if it was unliked
     */
    fun toggleLike(postId: PostId, userId: UserId): Boolean {
        return if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            unlikePost(postId, userId)
            false
        } else {
            likePost(postId, userId)
            true
        }
    }

    /**
     * Check if a user has liked a specific post
     * @param postId The ID of the post
     * @param userId The ID of the user
     * @return true if the user has liked the post, false otherwise
     */
    fun hasUserLikedPost(postId: PostId, userId: UserId): Boolean {
        return likeRepository.existsByPostIdAndUserId(postId, userId)
    }

    /**
     * Get the number of likes for a post
     * @param postId The ID of the post
     * @return The number of likes
     */
    fun getLikeCountForPost(postId: PostId): Long {
        return likeRepository.countByPostId(postId)
    }

    /**
     * Get all likes for a post
     * @param postId The ID of the post
     * @return List of likes for the post
     */
    fun getLikesForPost(postId: PostId): List<Like> {
        return likeRepository.findByPostId(postId)
    }

    /**
     * Get all likes by a user
     * @param userId The ID of the user
     * @return List of likes by the user
     */
    fun getLikesByUser(userId: UserId): List<Like> {
        return likeRepository.findByUserId(userId)
    }

    /**
     * Get the number of likes by a user
     * @param userId The ID of the user
     * @return The number of likes by the user
     */
    fun getLikeCountByUser(userId: UserId): Long {
        return likeRepository.countByUserId(userId)
    }
}

/**
 * Exception thrown when trying to create a duplicate like
 */
class DuplicateLikeException(message: String) : RuntimeException(message)

/**
 * Exception thrown when trying to unlike a post that wasn't liked
 */
class LikeNotFoundException(message: String) : RuntimeException(message)

