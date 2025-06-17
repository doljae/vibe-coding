package com.example.vibecoding.integration

import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.CreateCommentRequest
import com.example.vibecoding.presentation.dto.CreateReplyRequest
import com.example.vibecoding.presentation.dto.UpdateCommentRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class CommentApiIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var testPostId: PostId
    private lateinit var testUserId: UserId
    private lateinit var testCommentId: CommentId

    @BeforeEach
    fun setup() {
        // Create test data
        testPostId = PostId.generate()
        testUserId = UserId.generate()
        testCommentId = CommentId.generate()
    }

    @Test
    fun `should create comment successfully`() {
        // Given
        val request = CreateCommentRequest(
            content = "Test comment content",
            authorName = "Test Author",
            postId = testPostId.value.toString()
        )

        // When & Then
        val result = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("Test comment content"))
            .andExpect(jsonPath("$.authorName").value("Test Author"))
            .andExpect(jsonPath("$.postId").value(testPostId.value.toString()))
            .andExpect(jsonPath("$.id").exists())
            .andReturn()

        // Extract the comment ID for later tests
        val responseJson = objectMapper.readTree(result.response.contentAsString)
        val commentId = responseJson.get("id").asText()

        // Verify the comment exists
        mockMvc.perform(
            get("/api/comments/$commentId")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(commentId))
            .andExpect(jsonPath("$.content").value("Test comment content"))
    }

    @Test
    fun `should create reply successfully`() {
        // Given - First create a parent comment
        val parentRequest = CreateCommentRequest(
            content = "Parent comment content",
            authorName = "Test Author",
            postId = testPostId.value.toString()
        )

        val parentResult = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentRequest))
        )
            .andExpect(status().isCreated())
            .andReturn()

        val parentJson = objectMapper.readTree(parentResult.response.contentAsString)
        val parentId = parentJson.get("id").asText()

        // Then create a reply
        val replyRequest = CreateReplyRequest(
            content = "Reply comment content",
            authorName = "Reply Author",
            postId = testPostId.value.toString(),
            parentCommentId = parentId
        )

        // When & Then
        mockMvc.perform(
            post("/api/comments/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyRequest))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("Reply comment content"))
            .andExpect(jsonPath("$.authorName").value("Reply Author"))
            .andExpect(jsonPath("$.postId").value(testPostId.value.toString()))
            .andExpect(jsonPath("$.parentCommentId").value(parentId))
            .andExpect(jsonPath("$.isReply").value(true))
    }

    @Test
    fun `should get all comments for a post`() {
        // Given - Create two comments for the same post
        val request1 = CreateCommentRequest(
            content = "First comment content",
            authorName = "Test Author 1",
            postId = testPostId.value.toString()
        )

        val request2 = CreateCommentRequest(
            content = "Second comment content",
            authorName = "Test Author 2",
            postId = testPostId.value.toString()
        )

        mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))
        ).andExpect(status().isCreated())

        mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))
        ).andExpect(status().isCreated())

        // When & Then
        mockMvc.perform(
            get("/api/comments/posts/${testPostId.value}")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.postId").value(testPostId.value.toString()))
            .andExpect(jsonPath("$.comments").isArray())
            .andExpect(jsonPath("$.comments.length()").value(2))
            .andExpect(jsonPath("$.totalCommentCount").value(2))
    }

    @Test
    fun `should update comment successfully`() {
        // Given - First create a comment
        val createRequest = CreateCommentRequest(
            content = "Original comment content",
            authorName = "Test Author",
            postId = testPostId.value.toString()
        )

        val createResult = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated())
            .andReturn()

        val createJson = objectMapper.readTree(createResult.response.contentAsString)
        val commentId = createJson.get("id").asText()
        val authorId = createJson.get("authorId").asText()

        // Then update the comment
        val updateRequest = UpdateCommentRequest(
            content = "Updated comment content",
            authorId = authorId
        )

        // When & Then
        mockMvc.perform(
            put("/api/comments/$commentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(commentId))
            .andExpect(jsonPath("$.content").value("Updated comment content"))
            .andExpect(jsonPath("$.authorId").value(authorId))
    }

    @Test
    fun `should delete comment successfully`() {
        // Given - First create a comment
        val createRequest = CreateCommentRequest(
            content = "Comment to be deleted",
            authorName = "Test Author",
            postId = testPostId.value.toString()
        )

        val createResult = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated())
            .andReturn()

        val createJson = objectMapper.readTree(createResult.response.contentAsString)
        val commentId = createJson.get("id").asText()
        val authorId = createJson.get("authorId").asText()

        // When & Then
        mockMvc.perform(
            delete("/api/comments/$commentId")
                .param("authorId", authorId)
        )
            .andExpect(status().isNoContent())

        // Verify the comment no longer exists
        mockMvc.perform(
            get("/api/comments/$commentId")
        )
            .andExpect(status().isNotFound())
    }

    @Test
    fun `should delete comment with replies successfully`() {
        // Given - First create a parent comment
        val parentRequest = CreateCommentRequest(
            content = "Parent comment content",
            authorName = "Test Author",
            postId = testPostId.value.toString()
        )

        val parentResult = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parentRequest))
        )
            .andExpect(status().isCreated())
            .andReturn()

        val parentJson = objectMapper.readTree(parentResult.response.contentAsString)
        val parentId = parentJson.get("id").asText()
        val parentAuthorId = parentJson.get("authorId").asText()

        // Then create a reply
        val replyRequest = CreateReplyRequest(
            content = "Reply comment content",
            authorName = "Reply Author",
            postId = testPostId.value.toString(),
            parentCommentId = parentId
        )

        mockMvc.perform(
            post("/api/comments/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyRequest))
        )
            .andExpect(status().isCreated())

        // When & Then - Delete the parent comment
        mockMvc.perform(
            delete("/api/comments/$parentId")
                .param("authorId", parentAuthorId)
        )
            .andExpect(status().isNoContent())

        // Verify the parent comment no longer exists
        mockMvc.perform(
            get("/api/comments/$parentId")
        )
            .andExpect(status().isNotFound())

        // Verify the reply is also deleted (by checking the post's comment count)
        mockMvc.perform(
            get("/api/comments/posts/${testPostId.value}/count")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0))
    }

    @Test
    fun `should get comment count for a post`() {
        // Given - Create three comments for the same post
        val request1 = CreateCommentRequest(
            content = "First comment content",
            authorName = "Test Author 1",
            postId = testPostId.value.toString()
        )

        val request2 = CreateCommentRequest(
            content = "Second comment content",
            authorName = "Test Author 2",
            postId = testPostId.value.toString()
        )

        val request3 = CreateCommentRequest(
            content = "Third comment content",
            authorName = "Test Author 3",
            postId = testPostId.value.toString()
        )

        mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))
        ).andExpect(status().isCreated())

        mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))
        ).andExpect(status().isCreated())

        mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3))
        ).andExpect(status().isCreated())

        // When & Then
        mockMvc.perform(
            get("/api/comments/posts/${testPostId.value}/count")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(3))
    }

    @Test
    fun `should check if comment exists`() {
        // Given - First create a comment
        val createRequest = CreateCommentRequest(
            content = "Test comment content",
            authorName = "Test Author",
            postId = testPostId.value.toString()
        )

        val createResult = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated())
            .andReturn()

        val createJson = objectMapper.readTree(createResult.response.contentAsString)
        val commentId = createJson.get("id").asText()

        // When & Then
        mockMvc.perform(
            get("/api/comments/$commentId/exists")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true))

        // Check non-existent comment
        val nonExistentId = UUID.randomUUID().toString()
        mockMvc.perform(
            get("/api/comments/$nonExistentId/exists")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(false))
    }
}

