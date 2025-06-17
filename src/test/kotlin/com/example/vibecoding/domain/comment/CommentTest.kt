package com.example.vibecoding.domain.comment

import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class CommentTest {

    @Test
    fun `should create root comment successfully`() {
        // Given
        val commentId = CommentId.generate()
        val userId = UserId.generate()
        val postId = PostId.generate()
        val content = "Test comment"

        // When
        val comment = Comment.createRootComment(
            id = commentId,
            content = content,
            authorId = userId,
            postId = postId
        )

        // Then
        assertNotNull(comment)
        assertEquals(commentId, comment.id)
        assertEquals(content, comment.content)
        assertEquals(userId, comment.authorId)
        assertEquals(postId, comment.postId)
    }

    @Test
    fun `should create reply comment successfully`() {
        // Given
        val parentCommentId = CommentId.generate()
        val userId = UserId.generate()
        val postId = PostId.generate()
        val parentContent = "Parent comment"
        
        val parentComment = Comment.createRootComment(
            id = parentCommentId,
            content = parentContent,
            authorId = userId,
            postId = postId
        )
        
        val replyId = CommentId.generate()
        val replyContent = "Reply comment"

        // When
        val reply = Comment.createReply(
            id = replyId,
            content = replyContent,
            authorId = userId,
            postId = postId,
            parentComment = parentComment
        )

        // Then
        assertNotNull(reply)
        assertEquals(replyId, reply.id)
        assertEquals(replyContent, reply.content)
        assertEquals(userId, reply.authorId)
        assertEquals(postId, reply.postId)
        assertEquals(parentCommentId, reply.parentCommentId)
    }
}

