package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class InMemoryCommentRepositoryTest {

    private lateinit var repository: InMemoryCommentRepository
    
    private val userId = UserId.generate()
    private val postId = PostId.generate()
    private val commentId = CommentId.generate()
    private val validContent = "This is a valid comment"
    
    @BeforeEach
    fun setUp() {
        repository = InMemoryCommentRepository()
    }
    
    @Test
    fun `should save and find comment by id`() {
        // Given
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        
        // When
        val savedComment = repository.save(comment)
        val foundComment = repository.findById(commentId)
        
        // Then
        assertEquals(comment, savedComment)
        assertEquals(comment, foundComment)
    }
    
    @Test
    fun `should return null when finding non-existent comment`() {
        // When
        val foundComment = repository.findById(CommentId.generate())
        
        // Then
        assertNull(foundComment)
    }
    
    @Test
    fun `should find comments by post id`() {
        // Given
        val comment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Comment 1",
            authorId = userId,
            postId = postId
        )
        val comment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Comment 2",
            authorId = userId,
            postId = postId
        )
        val otherPostComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Other post comment",
            authorId = userId,
            postId = PostId.generate()
        )
        
        repository.save(comment1)
        repository.save(comment2)
        repository.save(otherPostComment)
        
        // When
        val comments = repository.findByPostId(postId)
        
        // Then
        assertEquals(2, comments.size)
        assertTrue(comments.contains(comment1))
        assertTrue(comments.contains(comment2))
        assertFalse(comments.contains(otherPostComment))
    }
    
    @Test
    fun `should find root comments by post id`() {
        // Given
        val rootComment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Root comment 1",
            authorId = userId,
            postId = postId
        )
        val rootComment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Root comment 2",
            authorId = userId,
            postId = postId
        )
        val reply = Comment.createReply(
            id = CommentId.generate(),
            content = "Reply to comment 1",
            authorId = userId,
            postId = postId,
            parentComment = rootComment1
        )
        
        repository.save(rootComment1)
        repository.save(rootComment2)
        repository.save(reply)
        
        // When
        val rootComments = repository.findRootCommentsByPostId(postId)
        
        // Then
        assertEquals(2, rootComments.size)
        assertTrue(rootComments.contains(rootComment1))
        assertTrue(rootComments.contains(rootComment2))
        assertFalse(rootComments.contains(reply))
    }
    
    @Test
    fun `should find replies by parent comment id`() {
        // Given
        val rootComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Root comment",
            authorId = userId,
            postId = postId
        )
        val reply1 = Comment.createReply(
            id = CommentId.generate(),
            content = "Reply 1",
            authorId = userId,
            postId = postId,
            parentComment = rootComment
        )
        val reply2 = Comment.createReply(
            id = CommentId.generate(),
            content = "Reply 2",
            authorId = userId,
            postId = postId,
            parentComment = rootComment
        )
        
        repository.save(rootComment)
        repository.save(reply1)
        repository.save(reply2)
        
        // When
        val replies = repository.findRepliesByParentCommentId(rootComment.id)
        
        // Then
        assertEquals(2, replies.size)
        assertTrue(replies.contains(reply1))
        assertTrue(replies.contains(reply2))
    }
    
    @Test
    fun `should delete comment by id`() {
        // Given
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        repository.save(comment)
        
        // When
        val deleted = repository.deleteById(commentId)
        
        // Then
        assertTrue(deleted)
        assertNull(repository.findById(commentId))
    }
    
    @Test
    fun `should return false when deleting non-existent comment`() {
        // When
        val deleted = repository.deleteById(CommentId.generate())
        
        // Then
        assertFalse(deleted)
    }
    
    @Test
    fun `should check if comment exists`() {
        // Given
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        repository.save(comment)
        
        // When
        val exists = repository.existsById(commentId)
        
        // Then
        assertTrue(exists)
    }
    
    @Test
    fun `should return false when checking if non-existent comment exists`() {
        // When
        val exists = repository.existsById(CommentId.generate())
        
        // Then
        assertFalse(exists)
    }
    
    @Test
    fun `should count comments by post id`() {
        // Given
        val comment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Comment 1",
            authorId = userId,
            postId = postId
        )
        val comment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Comment 2",
            authorId = userId,
            postId = postId
        )
        val otherPostComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Other post comment",
            authorId = userId,
            postId = PostId.generate()
        )
        
        repository.save(comment1)
        repository.save(comment2)
        repository.save(otherPostComment)
        
        // When
        val count = repository.countByPostId(postId)
        
        // Then
        assertEquals(2, count)
    }
    
    @Test
    fun `should find all comments`() {
        // Given
        val comment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Comment 1",
            authorId = userId,
            postId = postId
        )
        val comment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Comment 2",
            authorId = userId,
            postId = postId
        )
        
        repository.save(comment1)
        repository.save(comment2)
        
        // When
        val allComments = repository.findAll()
        
        // Then
        assertEquals(2, allComments.size)
        assertTrue(allComments.contains(comment1))
        assertTrue(allComments.contains(comment2))
    }
}

