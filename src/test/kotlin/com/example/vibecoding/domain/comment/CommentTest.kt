package com.example.vibecoding.domain.comment

import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CommentTest {

    @Test
    fun `should create a valid comment`() {
        // Given
        val commentId = CommentId.generate()
        val content = "This is a test comment"
        val authorId = UserId.generate()
        val postId = PostId.generate()
        val createdAt = LocalDateTime.now()
        val updatedAt = LocalDateTime.now()

        // When
        val comment = Comment(
            id = commentId,
            content = content,
            authorId = authorId,
            postId = postId,
            parentCommentId = null,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // Then
        assertEquals(commentId, comment.id)
        assertEquals(content, comment.content)
        assertEquals(authorId, comment.authorId)
        assertEquals(postId, comment.postId)
        assertNull(comment.parentCommentId)
        assertEquals(createdAt, comment.createdAt)
        assertEquals(updatedAt, comment.updatedAt)
    }

    @Test
    fun `should create a valid reply comment`() {
        // Given
        val commentId = CommentId.generate()
        val content = "This is a test reply"
        val authorId = UserId.generate()
        val postId = PostId.generate()
        val parentCommentId = CommentId.generate()
        val createdAt = LocalDateTime.now()
        val updatedAt = LocalDateTime.now()

        // When
        val comment = Comment(
            id = commentId,
            content = content,
            authorId = authorId,
            postId = postId,
            parentCommentId = parentCommentId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // Then
        assertEquals(commentId, comment.id)
        assertEquals(content, comment.content)
        assertEquals(authorId, comment.authorId)
        assertEquals(postId, comment.postId)
        assertEquals(parentCommentId, comment.parentCommentId)
        assertEquals(createdAt, comment.createdAt)
        assertEquals(updatedAt, comment.updatedAt)
    }

    @Test
    fun `should throw exception when content is blank`() {
        // Given
        val commentId = CommentId.generate()
        val content = ""  // Blank content
        val authorId = UserId.generate()
        val postId = PostId.generate()
        val createdAt = LocalDateTime.now()
        val updatedAt = LocalDateTime.now()

        // When & Then
        assertThrows<IllegalArgumentException> {
            Comment(
                id = commentId,
                content = content,
                authorId = authorId,
                postId = postId,
                parentCommentId = null,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

    @Test
    fun `should throw exception when content exceeds maximum length`() {
        // Given
        val commentId = CommentId.generate()
        val content = "a".repeat(1001)  // Exceeds 1000 characters
        val authorId = UserId.generate()
        val postId = PostId.generate()
        val createdAt = LocalDateTime.now()
        val updatedAt = LocalDateTime.now()

        // When & Then
        assertThrows<IllegalArgumentException> {
            Comment(
                id = commentId,
                content = content,
                authorId = authorId,
                postId = postId,
                parentCommentId = null,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

    @Test
    fun `isReply should return true for reply comments`() {
        // Given
        val comment = Comment(
            id = CommentId.generate(),
            content = "This is a reply",
            authorId = UserId.generate(),
            postId = PostId.generate(),
            parentCommentId = CommentId.generate(),  // Has parent comment
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When & Then
        assertTrue(comment.isReply())
        assertFalse(comment.isRootComment())
    }

    @Test
    fun `isRootComment should return true for root comments`() {
        // Given
        val comment = Comment(
            id = CommentId.generate(),
            content = "This is a root comment",
            authorId = UserId.generate(),
            postId = PostId.generate(),
            parentCommentId = null,  // No parent comment
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When & Then
        assertFalse(comment.isReply())
        assertTrue(comment.isRootComment())
    }

    @Test
    fun `updateContent should return new comment with updated content and timestamp`() {
        // Given
        val originalComment = Comment(
            id = CommentId.generate(),
            content = "Original content",
            authorId = UserId.generate(),
            postId = PostId.generate(),
            parentCommentId = null,
            createdAt = LocalDateTime.now().minusHours(1),
            updatedAt = LocalDateTime.now().minusHours(1)
        )
        val newContent = "Updated content"

        // When
        val updatedComment = originalComment.updateContent(newContent)

        // Then
        assertEquals(originalComment.id, updatedComment.id)
        assertEquals(newContent, updatedComment.content)
        assertEquals(originalComment.authorId, updatedComment.authorId)
        assertEquals(originalComment.postId, updatedComment.postId)
        assertEquals(originalComment.parentCommentId, updatedComment.parentCommentId)
        assertEquals(originalComment.createdAt, updatedComment.createdAt)
        assertTrue(updatedComment.updatedAt.isAfter(originalComment.updatedAt))
    }

    @Test
    fun `updateContent should throw exception when new content is blank`() {
        // Given
        val comment = Comment(
            id = CommentId.generate(),
            content = "Original content",
            authorId = UserId.generate(),
            postId = PostId.generate(),
            parentCommentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val newContent = ""  // Blank content

        // When & Then
        assertThrows<IllegalArgumentException> {
            comment.updateContent(newContent)
        }
    }

    @Test
    fun `updateContent should throw exception when new content exceeds maximum length`() {
        // Given
        val comment = Comment(
            id = CommentId.generate(),
            content = "Original content",
            authorId = UserId.generate(),
            postId = PostId.generate(),
            parentCommentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val newContent = "a".repeat(1001)  // Exceeds 1000 characters

        // When & Then
        assertThrows<IllegalArgumentException> {
            comment.updateContent(newContent)
        }
    }
}

