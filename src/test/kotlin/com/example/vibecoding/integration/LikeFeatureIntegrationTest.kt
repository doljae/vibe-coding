package com.example.vibecoding.integration

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
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
class LikeFeatureIntegrationTest {

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

    private lateinit var testUser: User
    private lateinit var testCategory: Category
    private lateinit var testPost: Post

    @BeforeEach
    fun setUp() {
        // Clear repositories
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
    fun `should like and unlike a post`() {
        // 1. Initially, the post should have 0 likes
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(0))

        // 2. Like the post
        mockMvc.perform(
            post("/api/likes/posts/${testPost.id.value}/users/${testUser.id.value}")
        )
            .andExpect(status().isCreated)

        // 3. Verify the post now has 1 like
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(1))

        // 4. Check if the user has liked the post
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}/users/${testUser.id.value}/status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hasLiked").value(true))

        // 5. Unlike the post
        mockMvc.perform(
            delete("/api/likes/posts/${testPost.id.value}/users/${testUser.id.value}")
        )
            .andExpect(status().isNoContent)

        // 6. Verify the post now has 0 likes again
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(0))

        // 7. Check if the user has unliked the post
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}/users/${testUser.id.value}/status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hasLiked").value(false))
    }

    @Test
    fun `should toggle like status`() {
        // 1. Initially, the post should have 0 likes
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(0))

        // 2. Toggle like (should like the post)
        mockMvc.perform(
            put("/api/likes/posts/${testPost.id.value}/users/${testUser.username}/toggle")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isLiked").value(true))

        // 3. Verify the post now has 1 like
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(1))

        // 4. Toggle like again (should unlike the post)
        mockMvc.perform(
            put("/api/likes/posts/${testPost.id.value}/users/${testUser.username}/toggle")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isLiked").value(false))

        // 5. Verify the post now has 0 likes again
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(0))
    }

    @Test
    fun `should handle multiple users liking the same post`() {
        // Create additional test users
        val testUser2 = User(
            id = UserId.generate(),
            username = "testuser2",
            email = "test2@example.com",
            displayName = "Test User 2",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(testUser2)

        val testUser3 = User(
            id = UserId.generate(),
            username = "testuser3",
            email = "test3@example.com",
            displayName = "Test User 3",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(testUser3)

        // 1. Initially, the post should have 0 likes
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(0))

        // 2. User 1 likes the post
        mockMvc.perform(
            post("/api/likes/posts/${testPost.id.value}/users/${testUser.id.value}")
        )
            .andExpect(status().isCreated)

        // 3. User 2 likes the post
        mockMvc.perform(
            post("/api/likes/posts/${testPost.id.value}/users/${testUser2.id.value}")
        )
            .andExpect(status().isCreated)

        // 4. User 3 likes the post
        mockMvc.perform(
            post("/api/likes/posts/${testPost.id.value}/users/${testUser3.id.value}")
        )
            .andExpect(status().isCreated)

        // 5. Verify the post now has 3 likes
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(3))

        // 6. Get all likes for the post
        mockMvc.perform(get("/api/likes/posts/${testPost.id.value}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(3))
    }
}

