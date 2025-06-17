package com.example.vibecoding.integration

import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.presentation.dto.CreatePostRequest
import com.fasterxml.jackson.databind.ObjectMapper
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

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class PostDeletionIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should delete post successfully`() {
        // Given - Create a post
        val createRequest = CreatePostRequest(
            title = "Test Post for Deletion",
            content = "This post will be deleted",
            authorName = "Test Author",
            categoryId = "550e8400-e29b-41d4-a716-446655440000" // Use a valid category ID
        )

        val createResult = mockMvc.perform(
            post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated())
            .andReturn()

        val createJson = objectMapper.readTree(createResult.response.contentAsString)
        val postId = createJson.get("id").asText()

        // When - Delete the post
        mockMvc.perform(
            delete("/api/posts/$postId")
        )
            .andExpect(status().isNoContent())

        // Then - Verify the post no longer exists
        mockMvc.perform(
            get("/api/posts/$postId")
        )
            .andExpect(status().isNotFound())
    }

    @Test
    fun `should return 404 when deleting non-existent post`() {
        // Given - A non-existent post ID
        val nonExistentId = PostId.generate().value.toString()

        // When & Then
        mockMvc.perform(
            delete("/api/posts/$nonExistentId")
        )
            .andExpect(status().isNotFound())
    }
}

