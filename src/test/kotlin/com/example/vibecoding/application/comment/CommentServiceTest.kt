package com.example.vibecoding.application.comment

import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentRepository
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CommentServiceTest {

    @Mock
    private lateinit var commentRepository: CommentRepository

    @Mock
    private lateinit var postRepository: PostRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var commentService: CommentService

    private val userId = UserId.generate()
    private val postId = PostId.generate()
    private val commentId = CommentId.generate()
    private val validContent = "This is a valid comment"

    @Test
    fun `should get comment successfully`() {
        // Given
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        
        `when`(commentRepository.findById(commentId)).thenReturn(comment)

        // When
        val result = commentService.getComment(commentId)

        // Then
        assertNotNull(result)
        assertEquals(commentId, result.id)
        assertEquals(validContent, result.content)
        assertEquals(userId, result.authorId)
        assertEquals(postId, result.postId)
        
        verify(commentRepository).findById(commentId)
    }

    @Test
    fun `should check if comment exists successfully`() {
        // Given
        `when`(commentRepository.existsById(commentId)).thenReturn(true)

        // When
        val result = commentService.commentExists(commentId)

        // Then
        assertTrue(result)
        verify(commentRepository).existsById(commentId)
    }

    @Test
    fun `should get comment count for post successfully`() {
        // Given
        val expectedCount = 5L
        `when`(commentRepository.countByPostId(postId)).thenReturn(expectedCount)

        // When
        val result = commentService.getCommentCountForPost(postId)

        // Then
        assertEquals(expectedCount, result)
        verify(commentRepository).countByPostId(postId)
    }

    @Test
    fun `should get comments for post successfully`() {
        // Given
        val rootComment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        
        val replyId = CommentId.generate()
        val reply = Comment.createReply(
            id = replyId,
            content = "Reply content",
            authorId = userId,
            postId = postId,
            parentComment = rootComment
        )
        
        `when`(postRepository.findById(postId)).thenReturn(mock())
        `when`(commentRepository.findRootCommentsByPostId(postId)).thenReturn(listOf(rootComment))
        `when`(commentRepository.findRepliesByParentCommentId(commentId)).thenReturn(listOf(reply))

        // When
        val result = commentService.getCommentsForPost(postId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(commentId, result[0].comment.id)
        assertEquals(1, result[0].replies.size)
        assertEquals(replyId, result[0].replies[0].id)
        
        verify(postRepository).findById(postId)
        verify(commentRepository).findRootCommentsByPostId(postId)
        verify(commentRepository).findRepliesByParentCommentId(commentId)
    }
}

