package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class InMemoryCommentRepositoryTest {

    private lateinit var repository: InMemoryCommentRepository
    private var postId: PostId = PostId(UUID.randomUUID())
    private var authorId: UserId = UserId(UUID.randomUUID())

    @BeforeEach
    fun setUp() {
        repository = InMemoryCommentRepository()
        postId = PostId.generate()
        authorId = UserId.generate()
    }

    @Test
    fun `save should store a comment and return it`() {
        // Given
        val comment = createComment("Test comment")

        // When
        val savedComment = repository.save(comment)

        // Then
        assertEquals(comment, savedComment)
        assertEquals(comment, repository.findById(comment.id))
    }

    @Test
    fun `findById should return the comment when it exists`() {
        // Given
        val comment = createComment("Test comment")
        repository.save(comment)

        // When
        val foundComment = repository.findById(comment.id)

        // Then
        assertNotNull(foundComment)
        assertEquals(comment, foundComment)
    }

    @Test
    fun `findById should return null when comment does not exist`() {
        // Given
        val nonExistentId = CommentId.generate()

        // When
        val foundComment = repository.findById(nonExistentId)

        // Then
        assertNull(foundComment)
    }

    @Test
    fun `findByPostId should return all comments for a post`() {
        // Given
        val comment1 = createComment("Comment 1")
        val comment2 = createComment("Comment 2")
        val comment3 = createComment("Comment 3", PostId.generate()) // Different post
        repository.save(comment1)
        repository.save(comment2)
        repository.save(comment3)

        // When
        val comments = repository.findByPostId(postId)

        // Then
        assertEquals(2, comments.size)
        assertTrue(comments.contains(comment1))
        assertTrue(comments.contains(comment2))
        assertFalse(comments.contains(comment3))
    }

    @Test
    fun `findRootCommentsByPostId should return only root comments for a post`() {
        // Given
        val rootComment1 = createComment("Root Comment 1")
        val rootComment2 = createComment("Root Comment 2")
        val replyComment = createComment("Reply Comment", postId, rootComment1.id)
        repository.save(rootComment1)
        repository.save(rootComment2)
        repository.save(replyComment)

        // When
        val rootComments = repository.findRootCommentsByPostId(postId)

        // Then
        assertEquals(2, rootComments.size)
        assertTrue(rootComments.contains(rootComment1))
        assertTrue(rootComments.contains(rootComment2))
        assertFalse(rootComments.contains(replyComment))
    }

    @Test
    fun `findRepliesByParentCommentId should return all replies to a comment`() {
        // Given
        val rootComment = createComment("Root Comment")
        val reply1 = createComment("Reply 1", postId, rootComment.id)
        val reply2 = createComment("Reply 2", postId, rootComment.id)
        val otherComment = createComment("Other Comment")
        repository.save(rootComment)
        repository.save(reply1)
        repository.save(reply2)
        repository.save(otherComment)

        // When
        val replies = repository.findRepliesByParentCommentId(rootComment.id)

        // Then
        assertEquals(2, replies.size)
        assertTrue(replies.contains(reply1))
        assertTrue(replies.contains(reply2))
        assertFalse(replies.contains(rootComment))
        assertFalse(replies.contains(otherComment))
    }

    @Test
    fun `deleteById should remove the comment and return true when it exists`() {
        // Given
        val comment = createComment("Test comment")
        repository.save(comment)

        // When
        val result = repository.deleteById(comment.id)

        // Then
        assertTrue(result)
        assertNull(repository.findById(comment.id))
    }

    @Test
    fun `deleteById should return false when comment does not exist`() {
        // Given
        val nonExistentId = CommentId.generate()

        // When
        val result = repository.deleteById(nonExistentId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `deleteByPostId should remove all comments for a post and return the count`() {
        // Given
        val comment1 = createComment("Comment 1")
        val comment2 = createComment("Comment 2")
        val comment3 = createComment("Comment 3", PostId.generate()) // Different post
        repository.save(comment1)
        repository.save(comment2)
        repository.save(comment3)

        // When
        val deletedCount = repository.deleteByPostId(postId)

        // Then
        assertEquals(2, deletedCount)
        assertNull(repository.findById(comment1.id))
        assertNull(repository.findById(comment2.id))
        assertNotNull(repository.findById(comment3.id))
    }

    @Test
    fun `existsById should return true when comment exists`() {
        // Given
        val comment = createComment("Test comment")
        repository.save(comment)

        // When
        val exists = repository.existsById(comment.id)

        // Then
        assertTrue(exists)
    }

    @Test
    fun `existsById should return false when comment does not exist`() {
        // Given
        val nonExistentId = CommentId.generate()

        // When
        val exists = repository.existsById(nonExistentId)

        // Then
        assertFalse(exists)
    }

    @Test
    fun `countByPostId should return the number of comments for a post`() {
        // Given
        val comment1 = createComment("Comment 1")
        val comment2 = createComment("Comment 2")
        val comment3 = createComment("Comment 3", PostId.generate()) // Different post
        repository.save(comment1)
        repository.save(comment2)
        repository.save(comment3)

        // When
        val count = repository.countByPostId(postId)

        // Then
        assertEquals(2, count)
    }

    @Test
    fun `findAll should return all comments`() {
        // Given
        val comment1 = createComment("Comment 1")
        val comment2 = createComment("Comment 2")
        val comment3 = createComment("Comment 3", PostId.generate())
        repository.save(comment1)
        repository.save(comment2)
        repository.save(comment3)

        // When
        val allComments = repository.findAll()

        // Then
        assertEquals(3, allComments.size)
        assertTrue(allComments.contains(comment1))
        assertTrue(allComments.contains(comment2))
        assertTrue(allComments.contains(comment3))
    }

    @Test
    fun `clear should remove all comments`() {
        // Given
        val comment1 = createComment("Comment 1")
        val comment2 = createComment("Comment 2")
        repository.save(comment1)
        repository.save(comment2)

        // When
        repository.clear()

        // Then
        assertTrue(repository.findAll().isEmpty())
    }

    // Helper method to create a comment
    private fun createComment(
        content: String,
        postId: PostId = this.postId,
        parentCommentId: CommentId? = null
    ): Comment {
        return Comment(
            id = CommentId.generate(),
            content = content,
            authorId = authorId,
            postId = postId,
            parentCommentId = parentCommentId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}

