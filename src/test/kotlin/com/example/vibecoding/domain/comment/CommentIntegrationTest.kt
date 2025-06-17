package com.example.vibecoding.domain.comment

import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class CommentIntegrationTest {

    @Test
    fun `should create root comment correctly`() {
        // Given
        val commentId = CommentId.generate()
        val content = "This is a test comment"
        val authorId = UserId.generate()
        val postId = PostId.generate()
        
        // When
        val comment = Comment.createRootComment(
            id = commentId,
            content = content,
            authorId = authorId,
            postId = postId
        )
        
        // Then
        assertEquals(commentId, comment.id)
        assertEquals(content, comment.content)
        assertEquals(authorId, comment.authorId)
        assertEquals(postId, comment.postId)
        assertNull(comment.parentCommentId)
        assertTrue(comment.isRootComment())
        assertFalse(comment.isReply())
    }
    
    @Test
    fun `should create reply comment correctly`() {
        // Given
        val parentCommentId = CommentId.generate()
        val parentComment = Comment.createRootComment(
            id = parentCommentId,
            content = "Parent comment",
            authorId = UserId.generate(),
            postId = PostId.generate()
        )
        
        val replyId = CommentId.generate()
        val replyContent = "This is a reply"
        val replyAuthorId = UserId.generate()
        
        // When
        val reply = Comment.createReply(
            id = replyId,
            content = replyContent,
            authorId = replyAuthorId,
            postId = parentComment.postId,
            parentComment = parentComment
        )
        
        // Then
        assertEquals(replyId, reply.id)
        assertEquals(replyContent, reply.content)
        assertEquals(replyAuthorId, reply.authorId)
        assertEquals(parentComment.postId, reply.postId)
        assertEquals(parentCommentId, reply.parentCommentId)
        assertFalse(reply.isRootComment())
        assertTrue(reply.isReply())
    }
    
    @Test
    fun `should update comment content correctly`() {
        // Given
        val comment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Original content",
            authorId = UserId.generate(),
            postId = PostId.generate()
        )
        val newContent = "Updated content"
        
        // When
        val updatedComment = comment.updateContent(newContent)
        
        // Then
        assertEquals(newContent, updatedComment.content)
        assertEquals(comment.id, updatedComment.id)
        assertEquals(comment.authorId, updatedComment.authorId)
        assertEquals(comment.postId, updatedComment.postId)
        assertEquals(comment.parentCommentId, updatedComment.parentCommentId)
        assertTrue(updatedComment.updatedAt.isAfter(comment.updatedAt) || 
                  updatedComment.updatedAt.isEqual(comment.updatedAt))
    }
    
    @Test
    fun `should throw exception when creating comment with empty content`() {
        // Given
        val emptyContent = ""
        
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            Comment.createRootComment(
                id = CommentId.generate(),
                content = emptyContent,
                authorId = UserId.generate(),
                postId = PostId.generate()
            )
        }
    }
    
    @Test
    fun `should throw exception when creating comment with too long content`() {
        // Given
        val tooLongContent = "a".repeat(Comment.MAX_CONTENT_LENGTH + 1)
        
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            Comment.createRootComment(
                id = CommentId.generate(),
                content = tooLongContent,
                authorId = UserId.generate(),
                postId = PostId.generate()
            )
        }
    }
}

