package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.category.CategoryService
import com.example.vibecoding.application.post.*
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.ImageAttachment
import com.example.vibecoding.domain.post.ImageAttachmentId
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.CreatePostRequest
import com.example.vibecoding.presentation.dto.UpdatePostRequest
import com.example.vibecoding.presentation.exception.GlobalExceptionHandler
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.io.ByteArrayInputStream
import java.time.LocalDateTime

class PostControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var postService: PostService
    private lateinit var userService: UserService
    private lateinit var categoryService: CategoryService
    private lateinit var imageStorageService: ImageStorageService
    private lateinit var objectMapper: ObjectMapper

    private val testUserId = UserId.generate()
    private val testCategoryId = CategoryId.generate()
    private val testPostId = PostId.generate()
    private val testImageId = ImageAttachmentId.generate()

    private lateinit var testUser: User
    private lateinit var testCategory: Category
    private lateinit var testPost: Post
    private lateinit var testImage: ImageAttachment

    @BeforeEach
    fun setUp() {
        postService = mockk(relaxed = true)
        userService = mockk(relaxed = true)
        categoryService = mockk(relaxed = true)
        imageStorageService = mockk(relaxed = true)
        objectMapper = ObjectMapper()

        val controller = PostController(
            postService,
            userService,
            categoryService,
            imageStorageService
        )

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(GlobalExceptionHandler())
            .build()

        // Set up test data
        val now = LocalDateTime.now()
        
        testUser = User(
            id = testUserId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = "Test bio",
            createdAt = now,
            updatedAt = now
        )
        
        testCategory = Category(
            id = testCategoryId,
            name = "Test Category",
            description = "Test category description",
            createdAt = now,
            updatedAt = now
        )
        
        testImage = ImageAttachment(
            id = testImageId,
            filename = "test-image.jpg",
            contentType = "image/jpeg",
            fileSizeBytes = 1024L,
            storagePath = "uploads/test-image.jpg",
            createdAt = now
        )
        
        testPost = Post(
            id = testPostId,
            title = "Test Post",
            content = "Test content",
            authorId = testUserId,
            categoryId = testCategoryId,
            imageAttachments = listOf(testImage),
            likeCount = 0,
            createdAt = now,
            updatedAt = now
        )
    }

    @Test
    fun `getAllPosts should return list of posts`() {
        // Given
        val posts = listOf(testPost)
        every { postService.getAllPosts() } returns posts
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(get("/api/posts"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$[0].title").value("Test Post"))
            .andExpect(jsonPath("$[0].content").value("Test content"))
            .andExpect(jsonPath("$[0].author.id").value(testUserId.value.toString()))
            .andExpect(jsonPath("$[0].category.id").value(testCategoryId.value.toString()))
            .andExpect(jsonPath("$[0].imageAttachments[0].id").value(testImageId.value.toString()))

        verify { postService.getAllPosts() }
    }

    @Test
    fun `getPostById should return post when found`() {
        // Given
        every { postService.getPostById(testPostId) } returns testPost
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(get("/api/posts/${testPostId.value}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$.title").value("Test Post"))
            .andExpect(jsonPath("$.content").value("Test content"))
            .andExpect(jsonPath("$.author.id").value(testUserId.value.toString()))
            .andExpect(jsonPath("$.category.id").value(testCategoryId.value.toString()))
            .andExpect(jsonPath("$.imageAttachments[0].id").value(testImageId.value.toString()))

        verify { postService.getPostById(testPostId) }
    }

    @Test
    fun `getPostById should return 404 when post not found`() {
        // Given
        every { postService.getPostById(testPostId) } throws PostNotFoundException("Post not found")

        // When & Then
        mockMvc.perform(get("/api/posts/${testPostId.value}"))
            .andExpect(status().isNotFound)

        verify { postService.getPostById(testPostId) }
    }

    @Test
    fun `createPost should create post successfully`() {
        // Given
        val request = CreatePostRequest(
            title = "Test Post",
            content = "Test content",
            authorId = testUserId.value.toString(),
            categoryId = testCategoryId.value.toString(),
            authorName = "Test User",
            category = "Test Category"
        )
        val postWithoutImages = testPost.copy(imageAttachments = emptyList())
        every { postService.createPost("Test Post", "Test content", testUserId, testCategoryId) } returns postWithoutImages
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(post("/api/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$.title").value("Test Post"))
            .andExpect(jsonPath("$.content").value("Test content"))
            .andExpect(jsonPath("$.author.id").value(testUserId.value.toString()))
            .andExpect(jsonPath("$.category.id").value(testCategoryId.value.toString()))

        verify { postService.createPost("Test Post", "Test content", testUserId, testCategoryId) }
    }

    @Test
    fun `createPost should return 400 for invalid request`() {
        // Given
        val request = CreatePostRequest(
            title = "", // Invalid: blank title
            content = "Test content",
            authorId = testUserId.value.toString(),
            categoryId = testCategoryId.value.toString(),
            authorName = "Test User",
            category = "Test Category"
        )
        
        // Mock service to throw IllegalArgumentException for invalid input
        every { postService.createPost("", "Test content", testUserId, testCategoryId) } throws IllegalArgumentException("Post title cannot be blank")

        // When & Then
        mockMvc.perform(post("/api/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { postService.createPost(any(), any(), any(), any()) }
    }

    @Test
    fun `updatePost should update post successfully`() {
        // Given
        val request = UpdatePostRequest(
            title = "Updated Title",
            content = "Updated content",
            categoryId = testCategoryId.value.toString()
        )
        val updatedPost = testPost.copy(
            title = "Updated Title",
            content = "Updated content"
        )
        every { postService.updatePost(testPostId, "Updated Title", "Updated content", testCategoryId) } returns updatedPost
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(put("/api/posts/${testPostId.value}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$.title").value("Updated Title"))
            .andExpect(jsonPath("$.content").value("Updated content"))

        verify { postService.updatePost(testPostId, "Updated Title", "Updated content", testCategoryId) }
    }

    @Test
    fun `updatePost should return 404 when post not found`() {
        // Given
        val request = UpdatePostRequest(
            title = "Updated Title",
            content = "Updated content"
        )
        every { postService.updatePost(testPostId, "Updated Title", "Updated content", null) } throws PostNotFoundException("Post not found")

        // When & Then
        mockMvc.perform(put("/api/posts/${testPostId.value}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound)

        verify { postService.updatePost(testPostId, "Updated Title", "Updated content", null) }
    }

    @Test
    fun `deletePost should delete post successfully`() {
        // Given
        every { postService.deletePost(testPostId, testUserId) } returns Unit

        // When & Then
        mockMvc.perform(delete("/api/posts/${testPostId.value}?authorId=${testUserId.value}"))
            .andExpect(status().isNoContent)

        verify { postService.deletePost(testPostId, testUserId) }
    }

    @Test
    fun `deletePost should return 404 when post not found`() {
        // Given
        every { postService.deletePost(testPostId, testUserId) } throws PostNotFoundException("Post not found")

        // When & Then
        mockMvc.perform(delete("/api/posts/${testPostId.value}?authorId=${testUserId.value}"))
            .andExpect(status().isNotFound)

        verify { postService.deletePost(testPostId, testUserId) }
    }

    @Test
    fun `addImageToPost should add image successfully`() {
        // Given
        val imageFile = MockMultipartFile(
            "image",
            "test-image.jpg",
            "image/jpeg",
            "test image content".toByteArray()
        )
        
        val imageUploadRequest = ImageUploadRequest(
            filename = "test-image.jpg",
            contentType = "image/jpeg",
            inputStream = ByteArrayInputStream(imageFile.bytes),
            fileSizeBytes = imageFile.size
        )
        
        every { imageStorageService.createImageUploadRequest(any()) } returns imageUploadRequest
        every { postService.attachImageToPost(testPostId, imageUploadRequest) } returns testPost
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(multipart("/api/posts/${testPostId.value}/images")
            .file(imageFile))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$.imageAttachments[0].id").value(testImageId.value.toString()))

        verify { postService.attachImageToPost(testPostId, imageUploadRequest) }
    }

    @Test
    fun `addImageToPost should return 404 when post not found`() {
        // Given
        val imageFile = MockMultipartFile(
            "image",
            "test-image.jpg",
            "image/jpeg",
            "test image content".toByteArray()
        )
        
        val imageUploadRequest = ImageUploadRequest(
            filename = "test-image.jpg",
            contentType = "image/jpeg",
            inputStream = ByteArrayInputStream(imageFile.bytes),
            fileSizeBytes = imageFile.size
        )
        
        every { imageStorageService.createImageUploadRequest(any()) } returns imageUploadRequest
        every { postService.attachImageToPost(testPostId, imageUploadRequest) } throws PostNotFoundException("Post not found")

        // When & Then
        mockMvc.perform(multipart("/api/posts/${testPostId.value}/images")
            .file(imageFile))
            .andExpect(status().isNotFound)

        verify { postService.attachImageToPost(testPostId, imageUploadRequest) }
    }

    @Test
    fun `getPostsByCategory should return posts for category`() {
        // Given
        val posts = listOf(testPost)
        every { postService.getPostsByCategory(testCategoryId) } returns posts
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(get("/api/posts/category/${testCategoryId.value}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$[0].title").value("Test Post"))

        verify { postService.getPostsByCategory(testCategoryId) }
    }

    @Test
    fun `searchPostsByTitle should return matching posts`() {
        // Given
        val searchTerm = "Test"
        val posts = listOf(testPost)
        every { postService.searchPostsByTitle(searchTerm) } returns posts
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(get("/api/posts/search?title=$searchTerm"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$[0].title").value("Test Post"))

        verify { postService.searchPostsByTitle(searchTerm) }
    }
}

