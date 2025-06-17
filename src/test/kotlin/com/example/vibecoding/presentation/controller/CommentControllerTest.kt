package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.*
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.CommentCreateRequest
import com.example.vibecoding.presentation.dto.CommentReplyRequest
import com.example.vibecoding.presentation.dto.CommentUpdateRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(CommentController::class)
class CommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var commentService: CommentService

    private val userId = UserId.generate()
    private val postId = PostId.generate()
    private val commentId = CommentId.generate()

    private lateinit var testComment: Comment
    private lateinit var testReply: Comment
    private lateinit var commentWithReplies: CommentWithReplies

    @BeforeEach
    fun setUp() {
        testComment = Comment(
            id = commentId,
            content = "Test comment",
            authorId = userId,
            postId = postId,
            parentCommentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        testReply = Comment(
            id = CommentId.generate(),
            content = "Test reply",
            authorId = userId,
            postId = postId,
            parentCommentId = commentId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        commentWithReplies = CommentWithReplies(
            comment = testComment,
            replies = listOf(testReply)
        )
    }

    @Test
    fun `should create comment successfully`() {
        // Given
        val request = CommentCreateRequest(
            content = "New comment",
            authorId = userId.value.toString(),
            postId = postId.value.toString()
        )

        every { 
            commentService.createComment(
                content = request.content,
                authorId = UserId.from(request.authorId),
                postId = PostId.from(request.postId)
            ) 
        } returns testComment

        // When & Then
        mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(testComment.id.value.toString()))
            .andExpect(jsonPath("$.content").value(testComment.content))

        verify { 
            commentService.createComment(
                content = request.content,
                authorId = UserId.from(request.authorId),
                postId = PostId.from(request.postId)
            ) 
        }
    }

    @Test
    fun `should return 400 when creating comment with invalid data`() {
        // Given
        val request = CommentCreateRequest(
            content = "",  // Invalid: empty content
            authorId = userId.value.toString(),
            postId = postId.value.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { commentService.createComment(any(), any(), any()) }
    }

    @Test
    fun `should create reply successfully`() {
        // Given
        val request = CommentReplyRequest(
            content = "New reply",
            authorId = userId.value.toString(),
            postId = postId.value.toString(),
            parentCommentId = commentId.value.toString()
        )

        every { 
            commentService.createReply(
                content = request.content,
                authorId = UserId.from(request.authorId),
                postId = PostId.from(request.postId),
                parentCommentId = CommentId.from(request.parentCommentId)
            ) 
        } returns testReply

        // When & Then
        mockMvc.perform(
            post("/api/comments/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(testReply.id.value.toString()))
            .andExpect(jsonPath("$.content").value(testReply.content))
            .andExpect(jsonPath("$.parentCommentId").value(testReply.parentCommentId?.value.toString()))

        verify { 
            commentService.createReply(
                content = request.content,
                authorId = UserId.from(request.authorId),
                postId = PostId.from(request.postId),
                parentCommentId = CommentId.from(request.parentCommentId)
            ) 
        }
    }

    @Test
    fun `should return 400 when creating reply with invalid data`() {
        // Given
        val request = CommentReplyRequest(
            content = "New reply",
            authorId = userId.value.toString(),
            postId = postId.value.toString(),
            parentCommentId = ""  // Invalid: empty parent comment ID
        )

        // When & Then
        mockMvc.perform(
            post("/api/comments/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { commentService.createReply(any(), any(), any(), any()) }
    }

    @Test
    fun `should get comment successfully`() {
        // Given
        every { commentService.getComment(commentId) } returns testComment

        // When & Then
        mockMvc.perform(get("/api/comments/${commentId.value}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testComment.id.value.toString()))
            .andExpect(jsonPath("$.content").value(testComment.content))

        verify { commentService.getComment(commentId) }
    }

    @Test
    fun `should return 404 when getting non-existent comment`() {
        // Given
        every { commentService.getComment(commentId) } throws CommentNotFoundException("Comment not found")

        // When & Then
        mockMvc.perform(get("/api/comments/${commentId.value}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Comment not found"))

        verify { commentService.getComment(commentId) }
    }

    @Test
    fun `should get comments for post successfully`() {
        // Given
        every { commentService.getCommentsForPost(postId) } returns listOf(commentWithReplies)

        // When & Then
        mockMvc.perform(get("/api/comments/post/${postId.value}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].comment.id").value(testComment.id.value.toString()))
            .andExpect(jsonPath("$[0].comment.content").value(testComment.content))
            .andExpect(jsonPath("$[0].replies[0].id").value(testReply.id.value.toString()))
            .andExpect(jsonPath("$[0].replies[0].content").value(testReply.content))

        verify { commentService.getCommentsForPost(postId) }
    }

    @Test
    fun `should update comment successfully`() {
        // Given
        val request = CommentUpdateRequest(
            content = "Updated content",
            authorId = userId.value.toString()
        )

        val updatedComment = testComment.copy(content = "Updated content")

        every { 
            commentService.updateComment(
                commentId = commentId,
                newContent = request.content,
                authorId = UserId.from(request.authorId)
            ) 
        } returns updatedComment

        // When & Then
        mockMvc.perform(
            put("/api/comments/${commentId.value}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(updatedComment.id.value.toString()))
            .andExpect(jsonPath("$.content").value(updatedComment.content))

        verify { 
            commentService.updateComment(
                commentId = commentId,
                newContent = request.content,
                authorId = UserId.from(request.authorId)
            ) 
        }
    }

    @Test
    fun `should return 403 when unauthorized user tries to update comment`() {
        // Given
        val request = CommentUpdateRequest(
            content = "Updated content",
            authorId = userId.value.toString()
        )

        every { 
            commentService.updateComment(
                commentId = commentId,
                newContent = request.content,
                authorId = UserId.from(request.authorId)
            ) 
        } throws UnauthorizedCommentModificationException("Unauthorized comment modification")

        // When & Then
        mockMvc.perform(
            put("/api/comments/${commentId.value}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("Unauthorized comment modification"))

        verify { 
            commentService.updateComment(
                commentId = commentId,
                newContent = request.content,
                authorId = UserId.from(request.authorId)
            ) 
        }
    }

    @Test
    fun `should delete comment successfully`() {
        // Given
        every { 
            commentService.deleteComment(
                commentId = commentId,
                authorId = userId
            ) 
        } returns true

        // When & Then
        mockMvc.perform(
            delete("/api/comments/${commentId.value}")
                .param("authorId", userId.value.toString())
        )
            .andExpect(status().isNoContent)

        verify { 
            commentService.deleteComment(
                commentId = commentId,
                authorId = userId
            ) 
        }
    }

    @Test
    fun `should return 403 when unauthorized user tries to delete comment`() {
        // Given
        every { 
            commentService.deleteComment(
                commentId = commentId,
                authorId = userId
            ) 
        } throws UnauthorizedCommentModificationException("Unauthorized comment deletion")

        // When & Then
        mockMvc.perform(
            delete("/api/comments/${commentId.value}")
                .param("authorId", userId.value.toString())
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("Unauthorized comment deletion"))

        verify { 
            commentService.deleteComment(
                commentId = commentId,
                authorId = userId
            ) 
        }
    }

    @Test
    fun `should return 404 when deleting non-existent comment`() {
        // Given
        every { 
            commentService.deleteComment(
                commentId = commentId,
                authorId = userId
            ) 
        } throws CommentNotFoundException("Comment not found")

        // When & Then
        mockMvc.perform(
            delete("/api/comments/${commentId.value}")
                .param("authorId", userId.value.toString())
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Comment not found"))

        verify { 
            commentService.deleteComment(
                commentId = commentId,
                authorId = userId
            ) 
        }
    }
}

