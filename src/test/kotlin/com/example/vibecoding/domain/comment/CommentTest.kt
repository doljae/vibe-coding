package com.example.vibecoding.domain.comment

import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class CommentTest {

    private val commentId = CommentId.generate()
    private val authorId = UserId.generate()
    private val postId = PostId.generate()
    private val validContent = "This is a valid comment content"
    private val createdAt = LocalDateTime.now()

    @Test
    fun `should create comment with valid data`() {
        val comment = Comment(
            id = commentId,
            content = validContent,
            authorId = authorId,
            postId = postId,
            parentCommentId = null,
            createdAt = createdAt,
            updatedAt = createdAt
        )

        assertEquals(commentId, comment.id)
        assertEquals(validContent, comment.content)
        assertEquals(authorId, comment.authorId)
        assertEquals(postId, comment.postId)
        assertNull(comment.parentCommentId)
        assertEquals(createdAt, comment.createdAt)
        assertEquals(createdAt, comment.updatedAt)
    }

    @Test
    fun `should throw exception when content is blank`() {
        assertThrows<IllegalArgumentException> {
            Comment(
                id = commentId,
                content = "",
                authorId = authorId,
                postId = postId,
                parentCommentId = null,
                createdAt = createdAt,
                updatedAt = createdAt
            )
        }
    }

    @Test
    fun `should throw exception when content is only whitespace`() {
        assertThrows<IllegalArgumentException> {
            Comment(
                id = commentId,
                content = "   ",
                authorId = authorId,
                postId = postId,
                parentCommentId = null,
                createdAt = createdAt,
                updatedAt = createdAt
            )
        }
    }

    @Test
    fun `should throw exception when content exceeds maximum length`() {
        val longContent = "a".repeat(Comment.MAX_CONTENT_LENGTH + 1)
        
        assertThrows<IllegalArgumentException> {
            Comment(
                id = commentId,
                content = longContent,
                authorId = authorId,
                postId = postId,
                parentCommentId = null,
                createdAt = createdAt,
                updatedAt = createdAt
            )
        }
    }

    @Test
    fun `should accept content at maximum length`() {
        val maxLengthContent = "a".repeat(Comment.MAX_CONTENT_LENGTH)
        
        val comment = Comment(
            id = commentId,
            content = maxLengthContent,
            authorId = authorId,
            postId = postId,
            parentCommentId = null,
            createdAt = createdAt,
            updatedAt = createdAt
        )

        assertEquals(maxLengthContent, comment.content)
    }

    @Test
    fun `should update content successfully`() {
        val comment = Comment(
            id = commentId,
            content = validContent,
            authorId = authorId,
            postId = postId,
            parentCommentId = null,
            createdAt = createdAt,
            updatedAt = createdAt
        )

        val newContent = "Updated comment content"
        val updatedComment = comment.updateContent(newContent)

        assertEquals(newContent, updatedComment.content)
        assertEquals(createdAt, updatedComment.createdAt) // createdAt should not change
        assertTrue(updatedComment.updatedAt.isAfter(createdAt)) // updatedAt should be updated
    }

    @Test
    fun `should throw exception when updating with blank content`() {
        val comment = Comment(
            id = commentId,
            content = validContent,
            authorId = authorId,
            postId = postId,
            parentCommentId = null,
            createdAt = createdAt,
            updatedAt = createdAt
        )

        assertThrows<IllegalArgumentException> {
            comment.updateContent("")
        }
    }

    @Test
    fun `should identify root comment correctly`() {
        val rootComment = Comment(
            id = commentId,
            content = validContent,
            authorId = authorId,
            postId = postId,
            parentCommentId = null,
            createdAt = createdAt,
            updatedAt = createdAt
        )

        assertTrue(rootComment.isRootComment())
        assertFalse(rootComment.isReply())
    }

    @Test
    fun `should identify reply comment correctly`() {
        val parentCommentId = CommentId.generate()
        val replyComment = Comment(
            id = commentId,
            content = validContent,
            authorId = authorId,
            postId = postId,
            parentCommentId = parentCommentId,
            createdAt = createdAt,
            updatedAt = createdAt
        )

        assertFalse(replyComment.isRootComment())
        assertTrue(replyComment.isReply())
        assertEquals(parentCommentId, replyComment.parentCommentId)
    }

    @Test
    fun `should create root comment using factory method`() {
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = authorId,
            postId = postId
        )

        assertEquals(commentId, comment.id)
        assertEquals(validContent, comment.content)
        assertEquals(authorId, comment.authorId)
        assertEquals(postId, comment.postId)
        assertNull(comment.parentCommentId)
        assertTrue(comment.isRootComment())
    }

    @Test
    fun `should create reply using factory method`() {
        val parentComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Parent comment",
            authorId = authorId,
            postId = postId
        )

        val reply = Comment.createReply(
            id = commentId,
            content = validContent,
            authorId = authorId,
            postId = postId,
            parentComment = parentComment
        )

        assertEquals(commentId, reply.id)
        assertEquals(validContent, reply.content)
        assertEquals(authorId, reply.authorId)
        assertEquals(postId, reply.postId)
        assertEquals(parentComment.id, reply.parentCommentId)
        assertTrue(reply.isReply())
    }

    @Test
    fun `should throw exception when creating reply to a reply`() {
        val rootComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Root comment",
            authorId = authorId,
            postId = postId
        )

        val firstReply = Comment.createReply(
            id = CommentId.generate(),
            content = "First reply",
            authorId = authorId,
            postId = postId,
            parentComment = rootComment
        )

        assertThrows<IllegalArgumentException> {
            Comment.createReply(
                id = commentId,
                content = validContent,
                authorId = authorId,
                postId = postId,
                parentComment = firstReply
            )
        }
    }

    @Test
    fun `should throw exception when reply and parent belong to different posts`() {
        val differentPostId = PostId.generate()
        val parentComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Parent comment",
            authorId = authorId,
            postId = differentPostId
        )

        assertThrows<IllegalArgumentException> {
            Comment.createReply(
                id = commentId,
                content = validContent,
                authorId = authorId,
                postId = postId, // Different from parent's postId
                parentComment = parentComment
            )
        }
    }

    @Test
    fun `should validate reply to parent comment successfully`() {
        val parentComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Parent comment",
            authorId = authorId,
            postId = postId
        )

        val reply = Comment(
            id = commentId,
            content = validContent,
            authorId = authorId,
            postId = postId,
            parentCommentId = parentComment.id,
            createdAt = createdAt,
            updatedAt = createdAt
        )

        // Should not throw exception
        assertDoesNotThrow {
            reply.validateAsReplyTo(parentComment)
        }
    }

    @Test
    fun `CommentId should generate unique IDs`() {
        val id1 = CommentId.generate()
        val id2 = CommentId.generate()
        
        assertNotEquals(id1, id2)
    }

    @Test
    fun `CommentId should create from string`() {
        val uuid = java.util.UUID.randomUUID()
        val commentId = CommentId.from(uuid.toString())
        
        assertEquals(uuid, commentId.value)
    }

    @Test
    fun `CommentId should throw exception for invalid string`() {
        assertThrows<IllegalArgumentException> {
            CommentId.from("invalid-uuid")
        }
    }
}

