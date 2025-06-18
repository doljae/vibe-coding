package com.example.vibecoding.integration

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import com.example.vibecoding.presentation.dto.CreateCommentRequest
import com.example.vibecoding.presentation.dto.CreateReplyRequest
import com.example.vibecoding.presentation.dto.UpdateCommentRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class CommentFeatureIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var commentRepository: CommentRepository

    private lateinit var testUser: User
    private lateinit var testCategory: Category
    private lateinit var testPost: Post

    @BeforeEach
    fun setUp() {
        // Clear repositories
        commentRepository.findAll().forEach { commentRepository.deleteById(it.id) }
        postRepository.findAll().forEach { postRepository.deleteById(it.id) }
        categoryRepository.findAll().forEach { categoryRepository.deleteById(it.id) }
        userRepository.findAll().forEach { userRepository.deleteById(it.id) }

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

        // Create test category
        testCategory = Category(
            id = CategoryId.generate(),
            name = "Test Category",
            description = "Test category description",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        categoryRepository.save(testCategory)

        // Create test post
        testPost = Post(
            id = PostId.generate(),
            title = "Test Post",
            content = "Test post content",
            authorId = testUser.id,
            categoryId = testCategory.id,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        postRepository.save(testPost)
    }

    @Test
    fun `should create, edit, and delete comments with proper UI updates`() {
        // 1. Create a root comment
        val createCommentRequest = CreateCommentRequest(
            content = "This is a test comment",
            authorName = testUser.username,
            postId = testPost.id.value.toString()
        )

        val createCommentResult = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCommentRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.content").value("This is a test comment"))
            .andExpect(jsonPath("$.authorName").value(testUser.displayName))
            .andReturn()

        val commentResponse = objectMapper.readValue(
            createCommentResult.response.contentAsString,
            Map::class.java
        )
        val commentId = commentResponse["id"] as String

        // 2. Create a reply to the comment
        val createReplyRequest = CreateReplyRequest(
            content = "This is a reply to the test comment",
            authorName = testUser.username,
            postId = testPost.id.value.toString(),
            parentCommentId = commentId
        )

        mockMvc.perform(
            post("/api/comments/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReplyRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.content").value("This is a reply to the test comment"))
            .andExpect(jsonPath("$.parentCommentId").value(commentId))

        // 3. Edit the root comment
        val updateCommentRequest = UpdateCommentRequest(
            content = "This is an updated test comment",
            authorId = testUser.id.value.toString()
        )

        mockMvc.perform(
            put("/api/comments/$commentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCommentRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("This is an updated test comment"))

        // 4. Get all comments for the post
        mockMvc.perform(get("/api/comments/posts/${testPost.id.value}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.comments[0].comment.content").value("This is an updated test comment"))
            .andExpect(jsonPath("$.comments[0].replies[0].content").value("This is a reply to the test comment"))
            .andExpect(jsonPath("$.totalCommentCount").value(2))

        // 5. Delete the root comment (should also delete the reply)
        mockMvc.perform(
            delete("/api/comments/$commentId")
                .param("authorId", testUser.id.value.toString())
        )
            .andExpect(status().isNoContent)

        // 6. Verify comments are deleted
        mockMvc.perform(get("/api/comments/posts/${testPost.id.value}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.comments").isEmpty)
            .andExpect(jsonPath("$.totalCommentCount").value(0))
    }

    @Test
    fun `should handle editing and deleting comments with replies`() {
        // 1. Create a root comment
        val rootComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Root comment",
            authorId = testUser.id,
            postId = testPost.id
        )
        commentRepository.save(rootComment)

        // 2. Create a reply to the root comment
        val replyComment = Comment.createReply(
            id = CommentId.generate(),
            content = "Reply to root comment",
            authorId = testUser.id,
            postId = testPost.id,
            parentComment = rootComment
        )
        commentRepository.save(replyComment)

        // 3. Edit the root comment
        val updateCommentRequest = UpdateCommentRequest(
            content = "Updated root comment",
            authorId = testUser.id.value.toString()
        )

        mockMvc.perform(
            put("/api/comments/${rootComment.id.value}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCommentRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("Updated root comment"))

        // 4. Verify the root comment was updated but the reply remains unchanged
        mockMvc.perform(get("/api/comments/posts/${testPost.id.value}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.comments[0].comment.content").value("Updated root comment"))
            .andExpect(jsonPath("$.comments[0].replies[0].content").value("Reply to root comment"))

        // 5. Delete the root comment (should also delete the reply)
        mockMvc.perform(
            delete("/api/comments/${rootComment.id.value}")
                .param("authorId", testUser.id.value.toString())
        )
            .andExpect(status().isNoContent)

        // 6. Verify both comments are deleted
        mockMvc.perform(get("/api/comments/posts/${testPost.id.value}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.comments").isEmpty)
            .andExpect(jsonPath("$.totalCommentCount").value(0))
    }
}

