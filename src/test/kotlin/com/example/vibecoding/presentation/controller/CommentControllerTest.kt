package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.application.comment.CommentWithReplies
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
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

    @MockkBean
    private lateinit var commentService: CommentService

    @MockBean
    private lateinit var userService: UserService

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

        every { commentService.getComment(commentId) } returns comment

        // When & Then
        mockMvc.perform(get("/api/comments/{commentId}", commentId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(commentId.value.toString()))
            .andExpect(jsonPath("$.content").value(validContent))
            .andExpect(jsonPath("$.authorId").value(userId.value.toString()))
            .andExpect(jsonPath("$.postId").value(postId.value.toString()))

        verify { commentService.getComment(commentId) }
    }

    @Test
    fun `should get comment count for post successfully`() {
        // Given
        val expectedCount = 5L
        every { commentService.getCommentCountForPost(postId) } returns expectedCount

        // When & Then
        mockMvc.perform(get("/api/comments/posts/{postId}/count", postId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(expectedCount))

        verify { commentService.getCommentCountForPost(postId) }
    }

    @Test
    fun `should check if comment exists successfully`() {
        // Given
        every { commentService.commentExists(commentId) } returns true

        // When & Then
        mockMvc.perform(get("/api/comments/{commentId}/exists", commentId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exists").value(true))

        verify { commentService.commentExists(commentId) }
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
        val reply = Comment.createReply(
            id = CommentId.generate(),
            content = "Reply content",
            authorId = userId,
            postId = postId,
            parentComment = rootComment
        )
        val commentWithReplies = CommentWithReplies(rootComment, listOf(reply))

        every { commentService.getCommentsForPost(postId) } returns listOf(commentWithReplies)
        every { commentService.getCommentCountForPost(postId) } returns 2L

        // When & Then
        mockMvc.perform(get("/api/comments/posts/{postId}", postId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.postId").value(postId.value.toString()))
            .andExpect(jsonPath("$.totalCommentCount").value(2))
            .andExpect(jsonPath("$.comments").isArray)
            .andExpect(jsonPath("$.comments[0].comment.id").value(commentId.value.toString()))
            .andExpect(jsonPath("$.comments[0].replies").isArray)
            .andExpect(jsonPath("$.comments[0].replyCount").value(1))

        verify { commentService.getCommentsForPost(postId) }
        verify { commentService.getCommentCountForPost(postId) }
    }
}
