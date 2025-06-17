package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentNotFoundException
import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.application.comment.CommentWithReplies
import com.example.vibecoding.application.comment.UnauthorizedCommentModificationException
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
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(CommentController::class)
class CommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var commentService: CommentService

    @MockkBean
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
        every { userService.getUserById(userId) } returns User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

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
        every { userService.getUserById(userId) } returns User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

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

    @Test
    fun `should create comment successfully`() {
        // Given
        val request = CreateCommentRequest(
            content = validContent,
            authorName = "testuser",
            postId = postId.value.toString()
        )

        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )

        every { userService.getUserByUsername("testuser") } returns user
        every { commentService.createComment(validContent, userId, postId) } returns comment
        every { userService.getUserById(userId) } returns user

        // When & Then
        mockMvc.perform(post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(commentId.value.toString()))
            .andExpect(jsonPath("$.content").value(validContent))
            .andExpect(jsonPath("$.authorId").value(userId.value.toString()))
            .andExpect(jsonPath("$.authorName").value("Test User"))
            .andExpect(jsonPath("$.postId").value(postId.value.toString()))

        verify { commentService.createComment(validContent, userId, postId) }
    }

    @Test
    fun `should create reply successfully`() {
        // Given
        val parentCommentId = CommentId.generate()
        val request = CreateReplyRequest(
            content = validContent,
            authorName = "testuser",
            postId = postId.value.toString(),
            parentCommentId = parentCommentId.value.toString()
        )

        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val reply = Comment.createReply(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId,
            parentComment = Comment.createRootComment(
                id = parentCommentId,
                content = "Parent comment",
                authorId = userId,
                postId = postId
            )
        )

        every { userService.getUserByUsername("testuser") } returns user
        every { commentService.createReply(validContent, userId, postId, parentCommentId) } returns reply
        every { userService.getUserById(userId) } returns user

        // When & Then
        mockMvc.perform(post("/api/comments/replies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(commentId.value.toString()))
            .andExpect(jsonPath("$.content").value(validContent))
            .andExpect(jsonPath("$.authorId").value(userId.value.toString()))
            .andExpect(jsonPath("$.authorName").value("Test User"))
            .andExpect(jsonPath("$.postId").value(postId.value.toString()))
            .andExpect(jsonPath("$.parentCommentId").value(parentCommentId.value.toString()))
            .andExpect(jsonPath("$.isReply").value(true))

        verify { commentService.createReply(validContent, userId, postId, parentCommentId) }
    }

    @Test
    fun `should update comment successfully`() {
        // Given
        val updatedContent = "Updated comment content"
        val request = UpdateCommentRequest(
            content = updatedContent,
            authorId = userId.value.toString()
        )

        val updatedComment = Comment.createRootComment(
            id = commentId,
            content = updatedContent,
            authorId = userId,
            postId = postId
        )

        every { commentService.updateComment(commentId, updatedContent, userId) } returns updatedComment
        every { userService.getUserById(userId) } returns User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When & Then
        mockMvc.perform(put("/api/comments/{commentId}", commentId.value.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(commentId.value.toString()))
            .andExpect(jsonPath("$.content").value(updatedContent))
            .andExpect(jsonPath("$.authorId").value(userId.value.toString()))

        verify { commentService.updateComment(commentId, updatedContent, userId) }
    }

    @Test
    fun `should delete comment successfully`() {
        // Given
        every { commentService.deleteComment(commentId, userId) } just runs

        // When & Then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId.value.toString())
            .param("authorId", userId.value.toString()))
            .andExpect(status().isNoContent)

        verify { commentService.deleteComment(commentId, userId) }
    }

    @Test
    fun `should return 404 when getting non-existent comment`() {
        // Given
        every { commentService.getComment(commentId) } throws CommentNotFoundException("Comment not found")

        // When & Then
        mockMvc.perform(get("/api/comments/{commentId}", commentId.value.toString()))
            .andExpect(status().isNotFound)

        verify { commentService.getComment(commentId) }
    }

    @Test
    fun `should return 403 when unauthorized user tries to update comment`() {
        // Given
        val updatedContent = "Updated comment content"
        val request = UpdateCommentRequest(
            content = updatedContent,
            authorId = userId.value.toString()
        )

        every { commentService.updateComment(commentId, updatedContent, userId) } throws 
            UnauthorizedCommentModificationException("User is not authorized to modify this comment")

        // When & Then
        mockMvc.perform(put("/api/comments/{commentId}", commentId.value.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden)

        verify { commentService.updateComment(commentId, updatedContent, userId) }
    }

    @Test
    fun `should return 403 when unauthorized user tries to delete comment`() {
        // Given
        every { commentService.deleteComment(commentId, userId) } throws 
            UnauthorizedCommentModificationException("User is not authorized to delete this comment")

        // When & Then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId.value.toString())
            .param("authorId", userId.value.toString()))
            .andExpect(status().isForbidden)

        verify { commentService.deleteComment(commentId, userId) }
    }

    @Test
    fun `should return 400 when creating comment with invalid data`() {
        // Given
        val request = CreateCommentRequest(
            content = "",  // Empty content is invalid
            authorName = "testuser",
            postId = postId.value.toString()
        )

        // When & Then
        mockMvc.perform(post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when creating reply with invalid data`() {
        // Given
        val request = CreateReplyRequest(
            content = validContent,
            authorName = "testuser",
            postId = postId.value.toString(),
            parentCommentId = ""  // Empty parent comment ID is invalid
        )

        // When & Then
        mockMvc.perform(post("/api/comments/replies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)
    }
}

