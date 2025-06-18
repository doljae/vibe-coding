package com.example.vibecoding.domain.comment

import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.booleans.shouldBeFalse
import org.junit.jupiter.api.Test
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

        comment.id shouldBe commentId
        comment.content shouldBe validContent
        comment.authorId shouldBe authorId
        comment.postId shouldBe postId
        comment.parentCommentId.shouldBeNull()
        comment.createdAt shouldBe createdAt
        comment.updatedAt shouldBe createdAt
    }

    @Test
    fun `should throw exception when content is blank`() {
        shouldThrow<IllegalArgumentException> {
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
        shouldThrow<IllegalArgumentException> {
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
        
        shouldThrow<IllegalArgumentException> {
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

        comment.content shouldBe maxLengthContent
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

        updatedComment.content shouldBe newContent
        updatedComment.createdAt shouldBe createdAt // createdAt should not change
        (updatedComment.updatedAt > createdAt).shouldBeTrue() // updatedAt should be updated
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

        shouldThrow<IllegalArgumentException> {
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

        rootComment.isRootComment().shouldBeTrue()
        rootComment.isReply().shouldBeFalse()
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

        replyComment.isRootComment().shouldBeFalse()
        replyComment.isReply().shouldBeTrue()
        replyComment.parentCommentId shouldBe parentCommentId
    }

    @Test
    fun `should create root comment using factory method`() {
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = authorId,
            postId = postId
        )

        comment.id shouldBe commentId
        comment.content shouldBe validContent
        comment.authorId shouldBe authorId
        comment.postId shouldBe postId
        comment.parentCommentId.shouldBeNull()
        comment.isRootComment().shouldBeTrue()
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

        reply.id shouldBe commentId
        reply.content shouldBe validContent
        reply.authorId shouldBe authorId
        reply.postId shouldBe postId
        reply.parentCommentId shouldBe parentComment.id
        reply.isReply().shouldBeTrue()
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

        shouldThrow<IllegalArgumentException> {
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

        shouldThrow<IllegalArgumentException> {
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
        shouldNotThrowAny {
            reply.validateAsReplyTo(parentComment)
        }
    }

    @Test
    fun `CommentId should generate unique IDs`() {
        val id1 = CommentId.generate()
        val id2 = CommentId.generate()
        
        id1 shouldNotBe id2
    }

    @Test
    fun `CommentId should create from string`() {
        val uuid = java.util.UUID.randomUUID()
        val commentId = CommentId.from(uuid.toString())
        
        commentId.value shouldBe uuid
    }

    @Test
    fun `CommentId should throw exception for invalid string`() {
        shouldThrow<IllegalArgumentException> {
            CommentId.from("invalid-uuid")
        }
    }
}

