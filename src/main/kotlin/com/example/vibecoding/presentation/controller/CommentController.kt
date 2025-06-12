package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST Controller for Comment operations
 */
@RestController
@RequestMapping("/api/comments")
class CommentController(
    private val commentService: CommentService
) {

    /**
     * Create a new comment on a post
     */
    @PostMapping
    fun createComment(@Valid @RequestBody request: CreateCommentRequest): ResponseEntity<CommentResponse> {
        val comment = commentService.createComment(
            content = request.content,
            authorId = request.toUserId(),
            postId = request.toPostId()
        )
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommentResponse.from(comment))
    }

    /**
     * Create a reply to an existing comment
     */
    @PostMapping("/replies")
    fun createReply(@Valid @RequestBody request: CreateReplyRequest): ResponseEntity<CommentResponse> {
        val reply = commentService.createReply(
            content = request.content,
            authorId = request.toUserId(),
            postId = request.toPostId(),
            parentCommentId = request.toParentCommentId()
        )
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommentResponse.from(reply))
    }

    /**
     * Get a specific comment by ID
     */
    @GetMapping("/{commentId}")
    fun getComment(@PathVariable commentId: String): ResponseEntity<CommentResponse> {
        val comment = commentService.getComment(CommentId.from(commentId))
        return ResponseEntity.ok(CommentResponse.from(comment))
    }

    /**
     * Update an existing comment
     */
    @PutMapping("/{commentId}")
    fun updateComment(
        @PathVariable commentId: String,
        @Valid @RequestBody request: UpdateCommentRequest
    ): ResponseEntity<CommentResponse> {
        val updatedComment = commentService.updateComment(
            commentId = CommentId.from(commentId),
            newContent = request.content,
            authorId = request.toUserId()
        )
        
        return ResponseEntity.ok(CommentResponse.from(updatedComment))
    }

    /**
     * Delete a comment
     */
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable commentId: String,
        @RequestParam authorId: String
    ): ResponseEntity<Void> {
        commentService.deleteComment(
            commentId = CommentId.from(commentId),
            authorId = UserId.from(authorId)
        )
        
        return ResponseEntity.noContent().build()
    }

    /**
     * Get all comments for a specific post
     */
    @GetMapping("/posts/{postId}")
    fun getCommentsForPost(@PathVariable postId: String): ResponseEntity<PostCommentsResponse> {
        val postIdObj = PostId.from(postId)
        val commentsWithReplies = commentService.getCommentsForPost(postIdObj)
        val totalCount = commentService.getCommentCountForPost(postIdObj)
        
        val response = PostCommentsResponse.from(postIdObj, commentsWithReplies, totalCount)
        return ResponseEntity.ok(response)
    }

    /**
     * Get comment count for a specific post
     */
    @GetMapping("/posts/{postId}/count")
    fun getCommentCountForPost(@PathVariable postId: String): ResponseEntity<Map<String, Long>> {
        val count = commentService.getCommentCountForPost(PostId.from(postId))
        return ResponseEntity.ok(mapOf("count" to count))
    }

    /**
     * Check if a comment exists
     */
    @GetMapping("/{commentId}/exists")
    fun commentExists(@PathVariable commentId: String): ResponseEntity<Map<String, Boolean>> {
        val exists = commentService.commentExists(CommentId.from(commentId))
        return ResponseEntity.ok(mapOf("exists" to exists))
    }
}
