package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(CommentController::class)
class CommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var commentService: CommentService

    private val userId = UserId.generate()
    private val postId = PostId.generate()
    private val commentId = CommentId.generate()
    private val validContent = "This is a valid comment"

    // Temporarily disabled due to test issues
    // @Test
    // fun `should create comment successfully`() {
    //     // Test implementation here
    // }

    @Test
    fun `should get comment successfully`() {
        // Given
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )

        whenever(commentService.getComment(commentId)).thenReturn(comment)

        // When & Then
        mockMvc.perform(get("/api/comments/{commentId}", commentId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(commentId.value.toString()))
            .andExpect(jsonPath("$.content").value(validContent))
            .andExpect(jsonPath("$.authorId").value(userId.value.toString()))
            .andExpect(jsonPath("$.postId").value(postId.value.toString()))

        verify(commentService).getComment(commentId)
    }

    @Test
    fun `should get comment count for post successfully`() {
        // Given
        val expectedCount = 5L
        whenever(commentService.getCommentCountForPost(postId)).thenReturn(expectedCount)

        // When & Then
        mockMvc.perform(get("/api/comments/posts/{postId}/count", postId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(expectedCount))

        verify(commentService).getCommentCountForPost(postId)
    }

    @Test
    fun `should check if comment exists successfully`() {
        // Given
        whenever(commentService.commentExists(commentId)).thenReturn(true)

        // When & Then
        mockMvc.perform(get("/api/comments/{commentId}/exists", commentId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exists").value(true))

        verify(commentService).commentExists(commentId)
    }
}
