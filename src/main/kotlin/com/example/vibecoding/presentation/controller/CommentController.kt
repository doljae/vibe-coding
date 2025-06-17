package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentNotFoundException
import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.application.comment.InvalidCommentReplyException
import com.example.vibecoding.application.comment.UnauthorizedCommentModificationException
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/comments")
class CommentController(private val commentService: CommentService) {

    @PostMapping
    fun createComment(@Valid @RequestBody request: CommentCreateRequest): ResponseEntity<CommentResponse> {
        val comment = commentService.createComment(
            content = request.content,
            authorId = UserId.from(request.authorId),
            postId = PostId.from(request.postId)
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment))
    }
    
    @PostMapping("/reply")
    fun createReply(@Valid @RequestBody request: CommentReplyRequest): ResponseEntity<CommentResponse> {
        val reply = commentService.createReply(
            content = request.content,
            authorId = UserId.from(request.authorId),
            postId = PostId.from(request.postId),
            parentCommentId = CommentId.from(request.parentCommentId)
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(reply))
    }
    
    @GetMapping("/{commentId}")
    fun getComment(@PathVariable commentId: String): ResponseEntity<CommentResponse> {
        val comment = commentService.getComment(CommentId.from(commentId))
        return ResponseEntity.ok(CommentResponse.from(comment))
    }
    
    @GetMapping("/post/{postId}")
    fun getCommentsForPost(@PathVariable postId: String): ResponseEntity<List<CommentWithRepliesResponse>> {
        val comments = commentService.getCommentsForPost(PostId.from(postId))
        val response = comments.map { CommentWithRepliesResponse.from(it) }
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/{commentId}")
    fun updateComment(
        @PathVariable commentId: String,
        @Valid @RequestBody request: CommentUpdateRequest
    ): ResponseEntity<CommentResponse> {
        val updatedComment = commentService.updateComment(
            commentId = CommentId.from(commentId),
            newContent = request.content,
            authorId = UserId.from(request.authorId)
        )
        
        return ResponseEntity.ok(CommentResponse.from(updatedComment))
    }
    
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable commentId: String,
        @RequestParam authorId: String
    ): ResponseEntity<Void> {
        val result = commentService.deleteComment(
            commentId = CommentId.from(commentId),
            authorId = UserId.from(authorId)
        )
        
        return ResponseEntity.noContent().build()
    }
    
    @ExceptionHandler(CommentNotFoundException::class)
    fun handleCommentNotFoundException(ex: CommentNotFoundException): ResponseEntity<SimpleErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleErrorResponse(ex.message ?: "Comment not found"))
    }
    
    @ExceptionHandler(UnauthorizedCommentModificationException::class)
    fun handleUnauthorizedCommentModificationException(ex: UnauthorizedCommentModificationException): ResponseEntity<SimpleErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(SimpleErrorResponse(ex.message ?: "Unauthorized comment modification"))
    }
    
    @ExceptionHandler(InvalidCommentReplyException::class)
    fun handleInvalidCommentReplyException(ex: InvalidCommentReplyException): ResponseEntity<SimpleErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SimpleErrorResponse(ex.message ?: "Invalid comment reply"))
    }
}

