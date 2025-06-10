package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.category.CategoryService
import com.example.vibecoding.application.post.PostService
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.CreateUserRequest
import com.example.vibecoding.presentation.dto.UpdateUserRequest
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

class UserControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var userService: UserService
    private lateinit var postService: PostService
    private lateinit var categoryService: CategoryService
    private lateinit var objectMapper: ObjectMapper

    private val testUserId = UserId.generate()
    private val testCategoryId = CategoryId.generate()
    private val testPostId = PostId.generate()
    
    private val testUser = User(
        id = testUserId,
        username = "testuser",
        email = "test@example.com",
        displayName = "Test User",
        bio = "Test bio",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private val testCategory = Category(
        id = testCategoryId,
        name = "Technology",
        description = "Tech posts",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private val testPost = Post(
        id = testPostId,
        title = "Test Post",
        content = "Test content",
        authorId = testUserId,
        categoryId = testCategoryId,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        userService = mockk()
        postService = mockk()
        categoryService = mockk()
        objectMapper = ObjectMapper()
        objectMapper.findAndRegisterModules()
        
        val controller = UserController(userService, postService, categoryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Test
    fun `getAllUsers should return list of users`() {
        // Given
        val users = listOf(testUser)
        every { userService.getAllUsers() } returns users

        // When & Then
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").value(testUserId.value.toString()))
            .andExpect(jsonPath("$[0].username").value("testuser"))
            .andExpect(jsonPath("$[0].displayName").value("Test User"))

        verify { userService.getAllUsers() }
    }

    @Test
    fun `getAllUsers should return empty list when no users exist`() {
        // Given
        every { userService.getAllUsers() } returns emptyList()

        // When & Then
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

        verify { userService.getAllUsers() }
    }

    @Test
    fun `getUserById should return user when found`() {
        // Given
        every { userService.getUserById(testUserId) } returns testUser

        // When & Then
        mockMvc.perform(get("/api/users/${testUserId.value}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testUserId.value.toString()))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.displayName").value("Test User"))
            .andExpect(jsonPath("$.bio").value("Test bio"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists())

        verify { userService.getUserById(testUserId) }
    }

    @Test
    fun `getUserById should return 400 when user not found`() {
        // Given
        every { userService.getUserById(testUserId) } throws IllegalArgumentException("User not found")

        // When & Then
        mockMvc.perform(get("/api/users/${testUserId.value}"))
            .andExpect(status().isBadRequest)

        verify { userService.getUserById(testUserId) }
    }

    @Test
    fun `getUserById should return 400 for invalid UUID`() {
        // When & Then
        mockMvc.perform(get("/api/users/invalid-uuid"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `createUser should create and return user`() {
        // Given
        val request = CreateUserRequest(
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = "Test bio"
        )
        every { userService.createUser("testuser", "test@example.com", "Test User", "Test bio") } returns testUser

        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testUserId.value.toString()))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.displayName").value("Test User"))
            .andExpect(jsonPath("$.bio").value("Test bio"))

        verify { userService.createUser("testuser", "test@example.com", "Test User", "Test bio") }
    }

    @Test
    fun `createUser should return 400 for invalid request`() {
        // Given
        val request = CreateUserRequest(
            username = "ab", // Invalid: too short
            email = "invalid-email",
            displayName = "Test User"
        )

        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `createUser should return 400 when username already exists`() {
        // Given
        val request = CreateUserRequest(
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User"
        )
        every { userService.createUser("testuser", "test@example.com", "Test User", null) } throws 
            IllegalArgumentException("Username already exists")

        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify { userService.createUser("testuser", "test@example.com", "Test User", null) }
    }

    @Test
    fun `updateUser should update and return user`() {
        // Given
        val request = UpdateUserRequest(
            displayName = "Updated User",
            email = "updated@example.com",
            bio = "Updated bio"
        )
        val updatedUser = testUser.copy(
            displayName = "Updated User",
            email = "updated@example.com",
            bio = "Updated bio"
        )
        
        every { userService.getUserById(testUserId) } returns testUser
        every { userService.updateUserDisplayName(testUserId, "Updated User") } returns updatedUser
        every { userService.updateUserEmail(testUserId, "updated@example.com") } returns updatedUser
        every { userService.updateUserBio(testUserId, "Updated bio") } returns updatedUser

        // When & Then
        mockMvc.perform(
            put("/api/users/${testUserId.value}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testUserId.value.toString()))
            .andExpect(jsonPath("$.displayName").value("Updated User"))
            .andExpect(jsonPath("$.email").value("updated@example.com"))
            .andExpect(jsonPath("$.bio").value("Updated bio"))

        verify { userService.updateUserDisplayName(testUserId, "Updated User") }
        verify { userService.updateUserEmail(testUserId, "updated@example.com") }
        verify { userService.updateUserBio(testUserId, "Updated bio") }
    }

    @Test
    fun `updateUser should return 400 when user not found`() {
        // Given
        val request = UpdateUserRequest(displayName = "Updated User")
        every { userService.getUserById(testUserId) } throws IllegalArgumentException("User not found")

        // When & Then
        mockMvc.perform(
            put("/api/users/${testUserId.value}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify { userService.getUserById(testUserId) }
    }

    @Test
    fun `deleteUser should delete user successfully`() {
        // Given
        every { userService.deleteUser(testUserId) } returns true

        // When & Then
        mockMvc.perform(delete("/api/users/${testUserId.value}"))
            .andExpect(status().isNoContent)

        verify { userService.deleteUser(testUserId) }
    }

    @Test
    fun `deleteUser should return 400 when user not found`() {
        // Given
        every { userService.deleteUser(testUserId) } throws IllegalArgumentException("User not found")

        // When & Then
        mockMvc.perform(delete("/api/users/${testUserId.value}"))
            .andExpect(status().isBadRequest)

        verify { userService.deleteUser(testUserId) }
    }

    @Test
    fun `getUserByUsername should return user when found`() {
        // Given
        every { userService.getUserByUsername("testuser") } returns testUser

        // When & Then
        mockMvc.perform(get("/api/users/search").param("username", "testuser"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testUserId.value.toString()))
            .andExpect(jsonPath("$.username").value("testuser"))

        verify { userService.getUserByUsername("testuser") }
    }

    @Test
    fun `getUserByUsername should return 400 when user not found`() {
        // Given
        every { userService.getUserByUsername("nonexistent") } throws IllegalArgumentException("User not found")

        // When & Then
        mockMvc.perform(get("/api/users/search").param("username", "nonexistent"))
            .andExpect(status().isBadRequest)

        verify { userService.getUserByUsername("nonexistent") }
    }

    @Test
    fun `getPostsByUser should return user posts`() {
        // Given
        val posts = listOf(testPost)
        every { userService.getUserById(testUserId) } returns testUser
        every { postService.getPostsByAuthor(testUserId) } returns posts
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(get("/api/users/${testUserId.value}/posts"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$[0].title").value("Test Post"))
            .andExpect(jsonPath("$[0].author.id").value(testUserId.value.toString()))
            .andExpect(jsonPath("$[0].category.id").value(testCategoryId.value.toString()))

        verify { userService.getUserById(testUserId) }
        verify { postService.getPostsByAuthor(testUserId) }
        verify { categoryService.getCategoryById(testCategoryId) }
    }

    @Test
    fun `checkUsernameAvailability should return true when available`() {
        // Given
        every { userService.isUsernameAvailable("newuser") } returns true

        // When & Then
        mockMvc.perform(get("/api/users/check-username").param("username", "newuser"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.available").value(true))

        verify { userService.isUsernameAvailable("newuser") }
    }

    @Test
    fun `checkUsernameAvailability should return false when not available`() {
        // Given
        every { userService.isUsernameAvailable("testuser") } returns false

        // When & Then
        mockMvc.perform(get("/api/users/check-username").param("username", "testuser"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.available").value(false))

        verify { userService.isUsernameAvailable("testuser") }
    }

    @Test
    fun `checkEmailAvailability should return true when available`() {
        // Given
        every { userService.isEmailAvailable("new@example.com") } returns true

        // When & Then
        mockMvc.perform(get("/api/users/check-email").param("email", "new@example.com"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.available").value(true))

        verify { userService.isEmailAvailable("new@example.com") }
    }

    @Test
    fun `checkEmailAvailability should return false when not available`() {
        // Given
        every { userService.isEmailAvailable("test@example.com") } returns false

        // When & Then
        mockMvc.perform(get("/api/users/check-email").param("email", "test@example.com"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.available").value(false))

        verify { userService.isEmailAvailable("test@example.com") }
    }
}

