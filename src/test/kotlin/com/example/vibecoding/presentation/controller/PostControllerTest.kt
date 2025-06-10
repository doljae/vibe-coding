package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.category.CategoryService
import com.example.vibecoding.application.post.ImageAttachmentException
import com.example.vibecoding.application.post.PostNotFoundException
import com.example.vibecoding.application.post.PostService
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.ImageAttachment
import com.example.vibecoding.domain.post.ImageId
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.CreatePostRequest
import com.example.vibecoding.presentation.dto.UpdatePostRequest
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
    private lateinit var objectMapper: ObjectMapper

    private val testUserId = UserId.generate()
    private val testCategoryId = CategoryId.generate()
    private val testPostId = PostId.generate()
    private val testImageId = ImageId.generate()
    
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

    private val testImageAttachment = ImageAttachment.create(
        filename = "test.jpg",
        storagePath = "/storage/test.jpg",
        contentType = "image/jpeg",
        fileSizeBytes = 1024L
    )

    private val testPost = Post(
        id = testPostId,
        title = "Test Post",
        content = "Test content",
        authorId = testUserId,
        categoryId = testCategoryId,
        imageAttachments = listOf(testImageAttachment),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        postService = mockk()
        userService = mockk()
        categoryService = mockk()
        objectMapper = ObjectMapper()
        objectMapper.findAndRegisterModules()
        
        val controller = PostController(postService, userService, categoryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
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
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$[0].title").value("Test Post"))
            .andExpect(jsonPath("$[0].author.id").value(testUserId.value.toString()))
            .andExpect(jsonPath("$[0].category.id").value(testCategoryId.value.toString()))
            .andExpect(jsonPath("$[0].imageCount").value(1))

        verify { postService.getAllPosts() }
        verify { userService.getUserById(testUserId) }
        verify { categoryService.getCategoryById(testCategoryId) }
    }

    @Test
    fun `getAllPosts should return empty list when no posts exist`() {
        // Given
        every { postService.getAllPosts() } returns emptyList()

        // When & Then
        mockMvc.perform(get("/api/posts"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

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
            .andExpect(jsonPath("$.imageAttachments").isArray)
            .andExpect(jsonPath("$.imageAttachments[0].filename").value("test.jpg"))

        verify { postService.getPostById(testPostId) }
        verify { userService.getUserById(testUserId) }
        verify { categoryService.getCategoryById(testCategoryId) }
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
    fun `getPostById should return 400 for invalid UUID`() {
        // When & Then
        mockMvc.perform(get("/api/posts/invalid-uuid"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `createPost should create and return post`() {
        // Given
        val request = CreatePostRequest(
            title = "Test Post",
            content = "Test content",
            authorId = testUserId.value.toString(),
            categoryId = testCategoryId.value.toString()
        )
        val postWithoutImages = testPost.copy(imageAttachments = emptyList())
        every { postService.createPost("Test Post", "Test content", testUserId, testCategoryId) } returns postWithoutImages
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(
            post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$.title").value("Test Post"))
            .andExpect(jsonPath("$.content").value("Test content"))
            .andExpect(jsonPath("$.imageAttachments").isEmpty)

        verify { postService.createPost("Test Post", "Test content", testUserId, testCategoryId) }
        verify { userService.getUserById(testUserId) }
        verify { categoryService.getCategoryById(testCategoryId) }
    }

    @Test
    fun `createPost should return 400 for invalid request`() {
        // Given
        val request = CreatePostRequest(
            title = "", // Invalid: blank title
            content = "Test content",
            authorId = testUserId.value.toString(),
            categoryId = testCategoryId.value.toString()
        )

        // When & Then
        mockMvc.perform(
            post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `createPostWithImages should create post with images`() {
        // Given
        val imageFile = MockMultipartFile(
            "images",
            "test.jpg",
            "image/jpeg",
            "test image content".toByteArray()
        )
        
        every { postService.createPostWithImages(any(), any(), any(), any(), any()) } returns testPost
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(
            multipart("/api/posts")
                .file(imageFile)
                .param("title", "Test Post")
                .param("content", "Test content")
                .param("authorId", testUserId.value.toString())
                .param("categoryId", testCategoryId.value.toString())
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$.title").value("Test Post"))
            .andExpect(jsonPath("$.imageAttachments").isArray)
            .andExpect(jsonPath("$.imageAttachments[0].filename").value("test.jpg"))

        verify { postService.createPostWithImages(any(), any(), any(), any(), any()) }
        verify { userService.getUserById(testUserId) }
        verify { categoryService.getCategoryById(testCategoryId) }
    }

    @Test
    fun `updatePost should update and return post`() {
        // Given
        val request = UpdatePostRequest(
            title = "Updated Post",
            content = "Updated content",
            categoryId = testCategoryId.value.toString()
        )
        val updatedPost = testPost.copy(
            title = "Updated Post",
            content = "Updated content"
        )
        every { postService.updatePost(testPostId, "Updated Post", "Updated content", testCategoryId) } returns updatedPost
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(
            put("/api/posts/${testPostId.value}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$.title").value("Updated Post"))
            .andExpect(jsonPath("$.content").value("Updated content"))

        verify { postService.updatePost(testPostId, "Updated Post", "Updated content", testCategoryId) }
        verify { userService.getUserById(testUserId) }
        verify { categoryService.getCategoryById(testCategoryId) }
    }

    @Test
    fun `updatePost should return 404 when post not found`() {
        // Given
        val request = UpdatePostRequest(title = "Updated Post")
        every { postService.updatePost(testPostId, "Updated Post", null, null) } throws 
            PostNotFoundException("Post not found")

        // When & Then
        mockMvc.perform(
            put("/api/posts/${testPostId.value}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)

        verify { postService.updatePost(testPostId, "Updated Post", null, null) }
    }

    @Test
    fun `deletePost should delete post successfully`() {
        // Given
        every { postService.deletePost(testPostId) } returns Unit

        // When & Then
        mockMvc.perform(delete("/api/posts/${testPostId.value}"))
            .andExpect(status().isNoContent)

        verify { postService.deletePost(testPostId) }
    }

    @Test
    fun `deletePost should return 404 when post not found`() {
        // Given
        every { postService.deletePost(testPostId) } throws PostNotFoundException("Post not found")

        // When & Then
        mockMvc.perform(delete("/api/posts/${testPostId.value}"))
            .andExpect(status().isNotFound)

        verify { postService.deletePost(testPostId) }
    }

    @Test
    fun `addImageToPost should add image successfully`() {
        // Given
        val imageFile = MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".toByteArray()
        )
        
        every { postService.attachImageToPost(any(), any()) } returns testPost

        // When & Then
        mockMvc.perform(
            multipart("/api/posts/${testPostId.value}/images")
                .file(imageFile)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.filename").value("test.jpg"))
            .andExpect(jsonPath("$.contentType").value("image/jpeg"))

        verify { postService.attachImageToPost(any(), any()) }
    }

    @Test
    fun `addImageToPost should return 400 when max images exceeded`() {
        // Given
        val imageFile = MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".toByteArray()
        )
        
        every { postService.attachImageToPost(any(), any()) } throws 
            ImageAttachmentException("Maximum images exceeded")

        // When & Then
        mockMvc.perform(
            multipart("/api/posts/${testPostId.value}/images")
                .file(imageFile)
        )
            .andExpect(status().isBadRequest)

        verify { postService.attachImageToPost(any(), any()) }
    }

    @Test
    fun `removeImageFromPost should remove image successfully`() {
        // Given
        every { postService.removeImageFromPost(testPostId, testImageId) } returns testPost

        // When & Then
        mockMvc.perform(delete("/api/posts/${testPostId.value}/images/${testImageId.value}"))
            .andExpect(status().isNoContent)

        verify { postService.removeImageFromPost(testPostId, testImageId) }
    }

    @Test
    fun `removeImageFromPost should return 400 when image not found`() {
        // Given
        every { postService.removeImageFromPost(testPostId, testImageId) } throws 
            ImageAttachmentException("Image not found")

        // When & Then
        mockMvc.perform(delete("/api/posts/${testPostId.value}/images/${testImageId.value}"))
            .andExpect(status().isBadRequest)

        verify { postService.removeImageFromPost(testPostId, testImageId) }
    }

    @Test
    fun `downloadImage should return image data`() {
        // Given
        val imageData = ByteArrayInputStream("test image content".toByteArray())
        every { postService.getPostImage(testPostId, testImageId) } returns testImageAttachment
        every { postService.getPostImageData(testPostId, testImageId) } returns imageData

        // When & Then
        mockMvc.perform(get("/api/posts/${testPostId.value}/images/${testImageId.value}"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "image/jpeg"))
            .andExpect(header().string("Content-Length", "1024"))

        verify { postService.getPostImage(testPostId, testImageId) }
        verify { postService.getPostImageData(testPostId, testImageId) }
    }

    @Test
    fun `downloadImage should return 400 when image not found`() {
        // Given
        every { postService.getPostImage(testPostId, testImageId) } throws 
            ImageAttachmentException("Image not found")

        // When & Then
        mockMvc.perform(get("/api/posts/${testPostId.value}/images/${testImageId.value}"))
            .andExpect(status().isBadRequest)

        verify { postService.getPostImage(testPostId, testImageId) }
    }

    @Test
    fun `searchPosts should return posts by title`() {
        // Given
        val posts = listOf(testPost)
        every { postService.searchPostsByTitle("Test") } returns posts
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(get("/api/posts/search").param("title", "Test"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$[0].title").value("Test Post"))

        verify { postService.searchPostsByTitle("Test") }
        verify { userService.getUserById(testUserId) }
        verify { categoryService.getCategoryById(testCategoryId) }
    }

    @Test
    fun `searchPosts should return posts by author`() {
        // Given
        val posts = listOf(testPost)
        every { postService.getPostsByAuthor(testUserId) } returns posts
        every { userService.getUserById(testUserId) } returns testUser
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(get("/api/posts/search").param("authorId", testUserId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").value(testPostId.value.toString()))

        verify { postService.getPostsByAuthor(testUserId) }
        verify { userService.getUserById(testUserId) }
        verify { categoryService.getCategoryById(testCategoryId) }
    }

    @Test
    fun `getPostsByCategory should return posts in category`() {
        // Given
        val posts = listOf(testPost)
        every { postService.getPostsByCategory(testCategoryId) } returns posts
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory
        every { userService.getUserById(testUserId) } returns testUser

        // When & Then
        mockMvc.perform(get("/api/posts/category/${testCategoryId.value}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").value(testPostId.value.toString()))
            .andExpect(jsonPath("$[0].category.id").value(testCategoryId.value.toString()))

        verify { postService.getPostsByCategory(testCategoryId) }
        verify { categoryService.getCategoryById(testCategoryId) }
        verify { userService.getUserById(testUserId) }
    }
}

