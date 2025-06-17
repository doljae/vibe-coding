package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.CreateCommentRequest
import com.example.vibecoding.presentation.dto.CreateReplyRequest
import com.example.vibecoding.presentation.dto.UpdateCommentRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

class CommentControllerOperationsTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var commentService: CommentService
    private lateinit var userService: UserService
    private lateinit var objectMapper: ObjectMapper
    private lateinit var commentController: CommentController

    private val userId = UserId.generate()
    private val postId = PostId.generate()
    private val commentId = CommentId.generate()
    private val validContent = "This is a valid comment"
    private val authorName = "testUser"

    @BeforeEach
    fun setUp() {
        commentService = mock(CommentService::class.java)
        userService = mock(UserService::class.java)
        objectMapper = ObjectMapper()
        commentController = CommentController(commentService, userService)
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build()
    }

    @Test
    fun `should create comment successfully`() {
        // Given
        val user = mock(User::class.java)
        `when`(user.id).thenReturn(userId)
        
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        
        val createRequest = CreateCommentRequest(
            content = validContent,
            authorName = authorName,
            postId = postId.value.toString()
        )
        
        `when`(userService.getUserByUsername(authorName)).thenReturn(user)
        `when`(commentService.createComment(validContent, userId, postId)).thenReturn(comment)

        // When & Then
        mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(commentId.value.toString()))
            .andExpect(jsonPath("$.content").value(validContent))
            .andExpect(jsonPath("$.authorId").value(userId.value.toString()))
            .andExpect(jsonPath("$.postId").value(postId.value.toString()))

        verify(commentService).createComment(validContent, userId, postId)
    }

    @Test
    fun `should create reply successfully`() {
        // Given
        val user = mock(User::class.java)
        `when`(user.id).thenReturn(userId)
        
        val parentComment = Comment.createRootComment(
            id = commentId,
            content = "Parent comment",
            authorId = userId,
            postId = postId
        )
        
        val replyId = CommentId.generate()
        val replyContent = "Reply content"
        
        val reply = Comment.createReply(
            id = replyId,
            content = replyContent,
            authorId = userId,
            postId = postId,
            parentComment = parentComment
        )
        
        val createReplyRequest = CreateReplyRequest(
            content = replyContent,
            authorName = authorName,
            postId = postId.value.toString(),
            parentCommentId = commentId.value.toString()
        )
        
        `when`(userService.getUserByUsername(authorName)).thenReturn(user)
        `when`(commentService.createReply(replyContent, userId, postId, commentId)).thenReturn(reply)

        // When & Then
        mockMvc.perform(
            post("/api/comments/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReplyRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(replyId.value.toString()))
            .andExpect(jsonPath("$.content").value(replyContent))
            .andExpect(jsonPath("$.authorId").value(userId.value.toString()))
            .andExpect(jsonPath("$.postId").value(postId.value.toString()))
            .andExpect(jsonPath("$.parentCommentId").value(commentId.value.toString()))

        verify(commentService).createReply(replyContent, userId, postId, commentId)
    }

    @Test
    fun `should update comment successfully`() {
        // Given
        val updatedContent = "Updated comment content"
        
        val originalComment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        
        val updatedComment = originalComment.updateContent(updatedContent)
        
        val updateRequest = UpdateCommentRequest(
            content = updatedContent,
            authorId = userId.value.toString()
        )
        
        `when`(commentService.updateComment(commentId, updatedContent, userId)).thenReturn(updatedComment)

        // When & Then
        mockMvc.perform(
            put("/api/comments/{commentId}", commentId.value.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(commentId.value.toString()))
            .andExpect(jsonPath("$.content").value(updatedContent))
            .andExpect(jsonPath("$.authorId").value(userId.value.toString()))

        verify(commentService).updateComment(commentId, updatedContent, userId)
    }

    @Test
    fun `should delete comment successfully`() {
        // Given
        doNothing().`when`(commentService).deleteComment(commentId, userId)

        // When & Then
        mockMvc.perform(
            delete("/api/comments/{commentId}", commentId.value.toString())
                .param("authorId", userId.value.toString())
        )
            .andExpect(status().isNoContent)

        verify(commentService).deleteComment(commentId, userId)
    }
}

