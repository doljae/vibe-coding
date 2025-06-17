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
 * Interface for Comment service operations
 */
interface CommentService {
    fun createComment(content: String, authorId: UserId, postId: PostId): Comment
    fun createReply(content: String, authorId: UserId, postId: PostId, parentCommentId: CommentId): Comment
    fun updateComment(commentId: CommentId, newContent: String, authorId: UserId): Comment
    fun deleteComment(commentId: CommentId, authorId: UserId)
    fun getComment(commentId: CommentId): Comment
    fun getCommentsForPost(postId: PostId): List<CommentWithReplies>
    fun getCommentCountForPost(postId: PostId): Long
    fun commentExists(commentId: CommentId): Boolean
}

/**
 * Implementation of CommentService
 */
@Service
class CommentServiceImpl(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : CommentService {

    /**
     * Create a new root comment on a post
     */
    override fun createComment(
        content: String,
        authorId: UserId,
        postId: PostId
    ): Comment {
        // Validate that the post exists
        postRepository.findById(postId)
            ?: throw PostNotFoundException("Post with id '$postId' not found")

        // Validate that the author exists
        userRepository.findById(authorId)
            ?: throw UserNotFoundException("User with id '$authorId' not found")

        val comment = Comment.createRootComment(
            id = CommentId.generate(),
            content = content,
            authorId = authorId,
            postId = postId
        )

        return commentRepository.save(comment)
    }

    /**
     * Create a reply to an existing comment
     */
    override fun createReply(
        content: String,
        authorId: UserId,
        postId: PostId,
        parentCommentId: CommentId
    ): Comment {
        // Validate that the post exists
        postRepository.findById(postId)
            ?: throw PostNotFoundException("Post with id '$postId' not found")

        // Validate that the author exists
        userRepository.findById(authorId)
            ?: throw UserNotFoundException("User with id '$authorId' not found")

        // Validate that the parent comment exists
        val parentComment = commentRepository.findById(parentCommentId)
            ?: throw CommentNotFoundException("Parent comment with id '$parentCommentId' not found")

        // Validate that the parent comment belongs to the same post
        if (parentComment.postId != postId) {
            throw InvalidCommentReplyException("Parent comment does not belong to the specified post")
        }

        val reply = Comment.createReply(
            id = CommentId.generate(),
            content = content,
            authorId = authorId,
            postId = postId,
            parentComment = parentComment
        )

        return commentRepository.save(reply)
    }

    /**
     * Update an existing comment
     */
    override fun updateComment(
        commentId: CommentId,
        newContent: String,
        authorId: UserId
    ): Comment {
        val existingComment = commentRepository.findById(commentId)
            ?: throw CommentNotFoundException("Comment with id '$commentId' not found")

        // Validate that the user is the author of the comment
        if (existingComment.authorId != authorId) {
            throw UnauthorizedCommentModificationException("User is not authorized to modify this comment")
        }

        val updatedComment = existingComment.updateContent(newContent)
        return commentRepository.save(updatedComment)
    }

    /**
     * Delete a comment
     */
    override fun deleteComment(commentId: CommentId, authorId: UserId) {
        val existingComment = commentRepository.findById(commentId)
            ?: throw CommentNotFoundException("Comment with id '$commentId' not found")

        // Validate that the user is the author of the comment
        if (existingComment.authorId != authorId) {
            throw UnauthorizedCommentModificationException("User is not authorized to delete this comment")
        }

        // Delete all replies to this comment first
        val replies = commentRepository.findRepliesByParentCommentId(commentId)
        replies.forEach { reply ->
            commentRepository.deleteById(reply.id)
        }

        // Delete the comment itself
        commentRepository.deleteById(commentId)
    }

    /**
     * Get a comment by ID
     */
    override fun getComment(commentId: CommentId): Comment {
        return commentRepository.findById(commentId)
            ?: throw CommentNotFoundException("Comment with id '$commentId' not found")
    }

    /**
     * Get all comments for a post with their replies organized hierarchically
     */
    override fun getCommentsForPost(postId: PostId): List<CommentWithReplies> {
        // Validate that the post exists
        postRepository.findById(postId)
            ?: throw PostNotFoundException("Post with id '$postId' not found")

        val rootComments = commentRepository.findRootCommentsByPostId(postId)
        
        return rootComments.map { rootComment ->
            val replies = commentRepository.findRepliesByParentCommentId(rootComment.id)
            CommentWithReplies(rootComment, replies)
        }
    }

    /**
     * Get comment count for a post
     */
    override fun getCommentCountForPost(postId: PostId): Long {
        return commentRepository.countByPostId(postId)
    }

    /**
     * Check if a comment exists
     */
    override fun commentExists(commentId: CommentId): Boolean {
        return commentRepository.existsById(commentId)
    }
}

/**
 * Data class to represent a comment with its replies
 */
data class CommentWithReplies(
    val comment: Comment,
    val replies: List<Comment>
)

// Exception classes
class CommentNotFoundException(message: String) : RuntimeException(message)
class PostNotFoundException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)
class UnauthorizedCommentModificationException(message: String) : RuntimeException(message)
class InvalidCommentReplyException(message: String) : RuntimeException(message)

