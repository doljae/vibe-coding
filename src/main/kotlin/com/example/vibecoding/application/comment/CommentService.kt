package com.example.vibecoding.application.comment

import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentRepository
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Data class to represent a comment with its replies
 */
data class CommentWithReplies(
    val comment: Comment,
    val replies: List<Comment>
)

/**
 * Service interface for comment operations
 */
interface CommentService {
    /**
     * Create a new comment
     */
    fun createComment(content: String, authorId: UserId, postId: PostId): Comment
    
    /**
     * Create a reply to an existing comment
     */
    fun createReply(content: String, authorId: UserId, postId: PostId, parentCommentId: CommentId): Comment
    
    /**
     * Get a comment by its ID
     */
    fun getComment(commentId: CommentId): Comment
    
    /**
     * Get all comments for a post, organized with replies
     */
    fun getCommentsForPost(postId: PostId): List<CommentWithReplies>
    
    /**
     * Update a comment's content
     */
    fun updateComment(commentId: CommentId, newContent: String, authorId: UserId): Comment
    
    /**
     * Delete a comment and all its replies
     */
    fun deleteComment(commentId: CommentId, authorId: UserId): Boolean
}

/**
 * Implementation of the CommentService interface
 */
@Service
class CommentServiceImpl(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : CommentService {
    
    override fun createComment(content: String, authorId: UserId, postId: PostId): Comment {
        // Validate that the post exists
        val post = postRepository.findById(postId) ?: throw PostNotFoundException("Post not found")
        
        // Validate that the user exists
        val user = userRepository.findById(authorId) ?: throw UserNotFoundException("User not found")
        
        // Create and save the comment
        val comment = Comment(
            id = CommentId.generate(),
            content = content,
            authorId = authorId,
            postId = postId,
            parentCommentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return commentRepository.save(comment)
    }
    
    override fun createReply(content: String, authorId: UserId, postId: PostId, parentCommentId: CommentId): Comment {
        // Validate that the parent comment exists
        val parentComment = commentRepository.findById(parentCommentId) 
            ?: throw CommentNotFoundException("Parent comment not found")
        
        // Validate that the post exists
        val post = postRepository.findById(postId) ?: throw PostNotFoundException("Post not found")
        
        // Validate that the user exists
        val user = userRepository.findById(authorId) ?: throw UserNotFoundException("User not found")
        
        // Validate that the parent comment is not already a reply
        if (parentComment.isReply()) {
            throw InvalidCommentReplyException("Cannot reply to a comment that is already a reply")
        }
        
        // Create and save the reply
        val reply = Comment(
            id = CommentId.generate(),
            content = content,
            authorId = authorId,
            postId = postId,
            parentCommentId = parentCommentId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return commentRepository.save(reply)
    }
    
    override fun getComment(commentId: CommentId): Comment {
        return commentRepository.findById(commentId) 
            ?: throw CommentNotFoundException("Comment not found")
    }
    
    override fun getCommentsForPost(postId: PostId): List<CommentWithReplies> {
        // Get all comments for the post
        val allComments = commentRepository.findByPostId(postId)
        
        // Separate root comments and replies
        val rootComments = allComments.filter { it.isRootComment() }
        val replies = allComments.filter { it.isReply() }
        
        // Group replies by parent comment ID
        val repliesByParentId = replies.groupBy { it.parentCommentId!! }
        
        // Create CommentWithReplies objects for each root comment
        return rootComments.map { rootComment ->
            CommentWithReplies(
                comment = rootComment,
                replies = repliesByParentId[rootComment.id] ?: emptyList()
            )
        }
    }
    
    override fun updateComment(commentId: CommentId, newContent: String, authorId: UserId): Comment {
        // Get the comment
        val comment = commentRepository.findById(commentId)
            ?: throw CommentNotFoundException("Comment not found")
        
        // Check if the user is authorized to update the comment
        if (comment.authorId != authorId) {
            throw UnauthorizedCommentModificationException("User is not authorized to modify this comment")
        }
        
        // Update the comment
        val updatedComment = comment.copy(
            content = newContent,
            updatedAt = LocalDateTime.now()
        )
        
        return commentRepository.save(updatedComment)
    }
    
    override fun deleteComment(commentId: CommentId, authorId: UserId): Boolean {
        // Get the comment
        val comment = commentRepository.findById(commentId)
            ?: throw CommentNotFoundException("Comment not found")
        
        // Check if the user is authorized to delete the comment
        if (comment.authorId != authorId) {
            throw UnauthorizedCommentModificationException("User is not authorized to delete this comment")
        }
        
        // If it's a root comment, delete all replies first
        if (comment.isRootComment()) {
            val replies = commentRepository.findRepliesByParentCommentId(commentId)
            for (reply in replies) {
                commentRepository.deleteById(reply.id)
            }
        }
        
        // Delete the comment itself
        return commentRepository.deleteById(commentId)
    }
}

// Exception classes
class CommentNotFoundException(message: String) : RuntimeException(message)
class PostNotFoundException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)
class UnauthorizedCommentModificationException(message: String) : RuntimeException(message)
class InvalidCommentReplyException(message: String) : RuntimeException(message)

