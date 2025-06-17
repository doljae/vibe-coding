package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentLike
import com.example.vibecoding.domain.comment.CommentLikeId
import com.example.vibecoding.domain.user.UserId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class InMemoryCommentLikeRepositoryTest {

    private lateinit var repository: InMemoryCommentLikeRepository
    private var commentId: CommentId = CommentId(UUID.randomUUID())
    private var userId: UserId = UserId(UUID.randomUUID())

    @BeforeEach
    fun setUp() {
        repository = InMemoryCommentLikeRepository()
        commentId = CommentId.generate()
        userId = UserId.generate()
    }

    @Test
    fun `save should store a comment like and return it`() {
        // Given
        val commentLike = createCommentLike()

        // When
        val savedCommentLike = repository.save(commentLike)

        // Then
        assertEquals(commentLike, savedCommentLike)
        assertEquals(commentLike, repository.findById(commentLike.id))
    }

    @Test
    fun `findById should return the comment like when it exists`() {
        // Given
        val commentLike = createCommentLike()
        repository.save(commentLike)

        // When
        val foundCommentLike = repository.findById(commentLike.id)

        // Then
        assertNotNull(foundCommentLike)
        assertEquals(commentLike, foundCommentLike)
    }

    @Test
    fun `findById should return null when comment like does not exist`() {
        // Given
        val nonExistentId = CommentLikeId.generate()

        // When
        val foundCommentLike = repository.findById(nonExistentId)

        // Then
        assertNull(foundCommentLike)
    }

    @Test
    fun `findByCommentId should return all likes for a comment`() {
        // Given
        val commentLike1 = createCommentLike()
        val commentLike2 = createCommentLike(userId = UserId.generate())
        val commentLike3 = createCommentLike(commentId = CommentId.generate()) // Different comment
        repository.save(commentLike1)
        repository.save(commentLike2)
        repository.save(commentLike3)

        // When
        val commentLikes = repository.findByCommentId(commentId)

        // Then
        assertEquals(2, commentLikes.size)
        assertTrue(commentLikes.contains(commentLike1))
        assertTrue(commentLikes.contains(commentLike2))
        assertFalse(commentLikes.contains(commentLike3))
    }

    @Test
    fun `findByUserId should return all likes by a user`() {
        // Given
        val commentLike1 = createCommentLike()
        val commentLike2 = createCommentLike(commentId = CommentId.generate())
        val commentLike3 = createCommentLike(userId = UserId.generate()) // Different user
        repository.save(commentLike1)
        repository.save(commentLike2)
        repository.save(commentLike3)

        // When
        val commentLikes = repository.findByUserId(userId)

        // Then
        assertEquals(2, commentLikes.size)
        assertTrue(commentLikes.contains(commentLike1))
        assertTrue(commentLikes.contains(commentLike2))
        assertFalse(commentLikes.contains(commentLike3))
    }

    @Test
    fun `findByCommentIdAndUserId should return the like when it exists`() {
        // Given
        val commentLike = createCommentLike()
        repository.save(commentLike)

        // When
        val foundCommentLike = repository.findByCommentIdAndUserId(commentId, userId)

        // Then
        assertNotNull(foundCommentLike)
        assertEquals(commentLike, foundCommentLike)
    }

    @Test
    fun `findByCommentIdAndUserId should return null when like does not exist`() {
        // Given
        val nonExistentCommentId = CommentId.generate()
        val nonExistentUserId = UserId.generate()

        // When
        val foundCommentLike = repository.findByCommentIdAndUserId(nonExistentCommentId, nonExistentUserId)

        // Then
        assertNull(foundCommentLike)
    }

    @Test
    fun `deleteById should remove the like and return true when it exists`() {
        // Given
        val commentLike = createCommentLike()
        repository.save(commentLike)

        // When
        val result = repository.deleteById(commentLike.id)

        // Then
        assertTrue(result)
        assertNull(repository.findById(commentLike.id))
    }

    @Test
    fun `deleteById should return false when like does not exist`() {
        // Given
        val nonExistentId = CommentLikeId.generate()

        // When
        val result = repository.deleteById(nonExistentId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `deleteByCommentIdAndUserId should remove the like and return true when it exists`() {
        // Given
        val commentLike = createCommentLike()
        repository.save(commentLike)

        // When
        val result = repository.deleteByCommentIdAndUserId(commentId, userId)

        // Then
        assertTrue(result)
        assertNull(repository.findById(commentLike.id))
    }

    @Test
    fun `deleteByCommentIdAndUserId should return false when like does not exist`() {
        // Given
        val nonExistentCommentId = CommentId.generate()
        val nonExistentUserId = UserId.generate()

        // When
        val result = repository.deleteByCommentIdAndUserId(nonExistentCommentId, nonExistentUserId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `deleteByCommentId should remove all likes for a comment and return the count`() {
        // Given
        val commentLike1 = createCommentLike()
        val commentLike2 = createCommentLike(userId = UserId.generate())
        val commentLike3 = createCommentLike(commentId = CommentId.generate()) // Different comment
        repository.save(commentLike1)
        repository.save(commentLike2)
        repository.save(commentLike3)

        // When
        val deletedCount = repository.deleteByCommentId(commentId)

        // Then
        assertEquals(2, deletedCount)
        assertNull(repository.findById(commentLike1.id))
        assertNull(repository.findById(commentLike2.id))
        assertNotNull(repository.findById(commentLike3.id))
    }

    @Test
    fun `countByCommentId should return the number of likes for a comment`() {
        // Given
        val commentLike1 = createCommentLike()
        val commentLike2 = createCommentLike(userId = UserId.generate())
        val commentLike3 = createCommentLike(commentId = CommentId.generate()) // Different comment
        repository.save(commentLike1)
        repository.save(commentLike2)
        repository.save(commentLike3)

        // When
        val count = repository.countByCommentId(commentId)

        // Then
        assertEquals(2, count)
    }

    @Test
    fun `existsByCommentIdAndUserId should return true when like exists`() {
        // Given
        val commentLike = createCommentLike()
        repository.save(commentLike)

        // When
        val exists = repository.existsByCommentIdAndUserId(commentId, userId)

        // Then
        assertTrue(exists)
    }

    @Test
    fun `existsByCommentIdAndUserId should return false when like does not exist`() {
        // Given
        val nonExistentCommentId = CommentId.generate()
        val nonExistentUserId = UserId.generate()

        // When
        val exists = repository.existsByCommentIdAndUserId(nonExistentCommentId, nonExistentUserId)

        // Then
        assertFalse(exists)
    }

    @Test
    fun `clear should remove all likes`() {
        // Given
        val commentLike1 = createCommentLike()
        val commentLike2 = createCommentLike(userId = UserId.generate())
        repository.save(commentLike1)
        repository.save(commentLike2)

        // When
        repository.clear()

        // Then
        assertNull(repository.findById(commentLike1.id))
        assertNull(repository.findById(commentLike2.id))
    }

    // Helper method to create a comment like
    private fun createCommentLike(
        commentId: CommentId = this.commentId,
        userId: UserId = this.userId
    ): CommentLike {
        return CommentLike(
            id = CommentLikeId.generate(),
            commentId = commentId,
            userId = userId,
            createdAt = LocalDateTime.now()
        )
    }
}

