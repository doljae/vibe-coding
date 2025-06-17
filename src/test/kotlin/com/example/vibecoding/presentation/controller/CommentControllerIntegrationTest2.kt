package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.infrastructure.repository.InMemoryCommentRepository
import com.example.vibecoding.infrastructure.repository.InMemoryPostRepository
import com.example.vibecoding.infrastructure.repository.InMemoryUserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerIntegrationTest2 {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User
    private lateinit var testPost: Post
    private lateinit var testComment: Comment

    @BeforeEach
    fun setUp() {
        // Clear repositories
        (commentRepository as InMemoryCommentRepository).clear()
        (postRepository as InMemoryPostRepository).clear()
        (userRepository as InMemoryUserRepository).clear()

        // Create test user
        testUser = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(testUser)

        // Create test post
        testPost = Post(
            id = PostId.generate(),
            title = "Test Post",
            content = "Test post content",
            authorId = testUser.id,
            categoryId = CategoryId.generate(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        postRepository.save(testPost)

        // Create test comment
        testComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Test comment content",
            authorId = testUser.id,
            postId = testPost.id
        )
        commentRepository.save(testComment)
    }

    @Test
    fun `should get comment successfully`() {
        // When & Then
        mockMvc.perform(get("/api/comments/{commentId}", testComment.id.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testComment.id.value.toString()))
            .andExpect(jsonPath("$.content").value(testComment.content))
            .andExpect(jsonPath("$.authorId").value(testUser.id.value.toString()))
            .andExpect(jsonPath("$.postId").value(testPost.id.value.toString()))
    }

    @Test
    fun `should get comment count for post successfully`() {
        // When & Then
        mockMvc.perform(get("/api/comments/posts/{postId}/count", testPost.id.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(1))
    }

    @Test
    fun `should check if comment exists successfully`() {
        // When & Then
        mockMvc.perform(get("/api/comments/{commentId}/exists", testComment.id.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exists").value(true))
    }

    @Test
    fun `should get comments for post successfully`() {
        // Given
        val reply = Comment.createReply(
            id = CommentId.generate(),
            content = "Reply content",
            authorId = testUser.id,
            postId = testPost.id,
            parentComment = testComment
        )
        commentRepository.save(reply)

        // When & Then
        mockMvc.perform(get("/api/comments/posts/{postId}", testPost.id.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.postId").value(testPost.id.value.toString()))
            .andExpect(jsonPath("$.totalCommentCount").value(2))
            .andExpect(jsonPath("$.comments").isArray)
            .andExpect(jsonPath("$.comments[0].comment.id").value(testComment.id.value.toString()))
            .andExpect(jsonPath("$.comments[0].replies").isArray)
            .andExpect(jsonPath("$.comments[0].replyCount").value(1))
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun commentRepository(): CommentRepository {
            return InMemoryCommentRepository()
        }

        @Bean
        @Primary
        fun postRepository(): PostRepository {
            return InMemoryPostRepository()
        }

        @Bean
        @Primary
        fun userRepository(): UserRepository {
            return InMemoryUserRepository()
        }
    }
}

