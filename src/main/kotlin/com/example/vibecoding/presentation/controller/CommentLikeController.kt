package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.*
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.CommentLikeRequest
import com.example.vibecoding.presentation.dto.CommentLikeResponse
import com.example.vibecoding.presentation.dto.SimpleErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/comments")
class CommentLikeController(private val commentLikeService: CommentLikeService) {

    @PostMapping("/{commentId}/like")
    fun likeComment(
        @PathVariable commentId: String,
        @RequestBody request: CommentLikeRequest
    ): ResponseEntity<CommentLikeResponse> {
        val commentLike = commentLikeService.likeComment(
            commentId = CommentId.from(commentId),
            userId = UserId.from(request.userId)
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            CommentLikeResponse(
                id = commentLike.id.value.toString(),
                commentId = commentLike.commentId.value.toString(),
                userId = commentLike.userId.value.toString(),
                createdAt = commentLike.createdAt
            )
        )
    }
    
    @DeleteMapping("/{commentId}/like")
    fun unlikeComment(
        @PathVariable commentId: String,
        @RequestParam userId: String
    ): ResponseEntity<Void> {
        val result = commentLikeService.unlikeComment(
            commentId = CommentId.from(commentId),
            userId = UserId.from(userId)
        )
        
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/{commentId}/like/count")
    fun getLikeCount(@PathVariable commentId: String): ResponseEntity<Map<String, Long>> {
        val count = commentLikeService.getLikeCount(CommentId.from(commentId))
        return ResponseEntity.ok(mapOf("count" to count))
    }
    
    @GetMapping("/{commentId}/like/status")
    fun checkLikeStatus(
        @PathVariable commentId: String,
        @RequestParam userId: String
    ): ResponseEntity<Map<String, Boolean>> {
        val hasLiked = commentLikeService.hasUserLikedComment(
            commentId = CommentId.from(commentId),
            userId = UserId.from(userId)
        )
        
        return ResponseEntity.ok(mapOf("hasLiked" to hasLiked))
    }
    
    @ExceptionHandler(CommentNotFoundException::class)
    fun handleCommentNotFoundException(ex: CommentNotFoundException): ResponseEntity<SimpleErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleErrorResponse(ex.message ?: "Comment not found"))
    }
    
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<SimpleErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleErrorResponse(ex.message ?: "User not found"))
    }
    
    @ExceptionHandler(CommentAlreadyLikedException::class)
    fun handleCommentAlreadyLikedException(ex: CommentAlreadyLikedException): ResponseEntity<SimpleErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(SimpleErrorResponse(ex.message ?: "Comment already liked"))
    }
}

