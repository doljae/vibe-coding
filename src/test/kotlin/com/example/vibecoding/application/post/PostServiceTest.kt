package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.*
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.time.LocalDateTime

class PostServiceTest {

    private lateinit var postRepository: PostRepository
    private lateinit var userRepository: UserRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var imageStorageService: ImageStorageService
    private lateinit var likeRepository: LikeRepository
    private lateinit var postService: PostService

    @BeforeEach
    fun setUp() {
        postRepository = mockk()
        categoryRepository = mockk()
        userRepository = mockk()
        imageStorageService = mockk()
        likeRepository = mockk()
        postService = PostService(
            postRepository,
            categoryRepository,
            userRepository,
            imageStorageService,
            likeRepository
        )
    }

    @Test
    fun `should create post successfully`() {
        // Given
        val postId = PostId.generate()
        val title = "Test Post"
        val content = "Test Content"
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        val post = createTestPost(postId, title, content, authorId, categoryId)
        
        every { userRepository.findById(authorId) } returns mockk()
        every { categoryRepository.existsById(categoryId) } returns true
        every { postRepository.save(any()) } returns post

        // When
        val result = postService.createPost(title, content, authorId, categoryId)

        // Then
        assertNotNull(result)
        assertEquals(title, result.title)
        assertEquals(content, result.content)
        assertEquals(authorId, result.authorId)
        assertEquals(categoryId, result.categoryId)
        
        verify { userRepository.findById(authorId) }
        verify { categoryRepository.existsById(categoryId) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should get post by id`() {
        // Given
        val postId = PostId.generate()
        val title = "Test Post"
        val content = "Test Content"
        val authorId = UserId.generate()
        val post = createTestPost(postId, title, content, authorId)
        
        every { postRepository.findById(postId) } returns post

        // When
        val result = postService.getPostById(postId)

        // Then
        assertNotNull(result)
        assertEquals(postId, result.id)
        assertEquals(title, result.title)
        assertEquals(content, result.content)
        assertEquals(authorId, result.authorId)
        
        verify { postRepository.findById(postId) }
    }

    @Test
    fun `should throw exception when post not found`() {
        // Given
        val postId = PostId.generate()
        
        every { postRepository.findById(postId) } returns null

        // When/Then
        assertThrows<PostNotFoundException> {
            postService.getPostById(postId)
        }
        
        verify { postRepository.findById(postId) }
    }

    @Test
    fun `should update post successfully`() {
        // Given
        val postId = PostId.generate()
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        val title = "Updated Title"
        val content = "Updated Content"
        val post = createTestPost(postId, "Original Title", "Original Content", authorId)
        
        every { postRepository.findById(postId) } returns post
        every { categoryRepository.existsById(categoryId) } returns true
        every { postRepository.save(any()) } returnsArgument 0

        // When
        val result = postService.updatePost(postId, title, content, categoryId)

        // Then
        assertNotNull(result)
        assertEquals(title, result.title)
        assertEquals(content, result.content)
        assertEquals(categoryId, result.categoryId)
        
        verify { postRepository.findById(postId) }
        verify { categoryRepository.existsById(categoryId) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should get all posts`() {
        // Given
        val posts = listOf(
            createTestPost(PostId.generate(), "Post 1", "Content 1", UserId.generate()),
            createTestPost(PostId.generate(), "Post 2", "Content 2", UserId.generate())
        )
        
        every { postRepository.findAll() } returns posts

        // When
        val result = postService.getAllPosts()

        // Then
        assertNotNull(result)
        assertEquals(posts, result)
        
        verify { postRepository.findAll() }
    }

    @Test
    fun `should get posts by category`() {
        // Given
        val categoryId = CategoryId.generate()
        val posts = listOf(
            createTestPost(PostId.generate(), "Post 1", "Content 1", UserId.generate(), categoryId),
            createTestPost(PostId.generate(), "Post 2", "Content 2", UserId.generate(), categoryId)
        )
        
        every { categoryRepository.existsById(categoryId) } returns true
        every { postRepository.findByCategoryId(categoryId) } returns posts

        // When
        val result = postService.getPostsByCategory(categoryId)

        // Then
        assertEquals(posts, result)
        
        verify { categoryRepository.existsById(categoryId) }
        verify { postRepository.findByCategoryId(categoryId) }
    }

    @Test
    fun `should search posts by title`() {
        // Given
        val title = "Test"
        val posts = listOf(
            createTestPost(PostId.generate(), "Test Post 1", "Content 1", UserId.generate()),
            createTestPost(PostId.generate(), "Test Post 2", "Content 2", UserId.generate())
        )
        
        every { postRepository.findByTitle(title) } returns posts

        // When
        val result = postService.searchPostsByTitle(title)

        // Then
        assertNotNull(result)
        assertEquals(posts, result)
        
        verify { postRepository.findByTitle(title) }
    }

    @Test
    fun `should delete post successfully`() {
        // Given
        val postId = PostId.generate()
        val authorId = UserId.generate()
        val post = createTestPost(postId, "Test Post", "Test Content", authorId)
        
        every { postRepository.findById(postId) } returns post
        every { postRepository.delete(postId) } returns true

        // When
        postService.deletePost(postId, authorId)

        // Then
        verify { postRepository.findById(postId) }
        verify { postRepository.delete(postId) }
    }

    @Test
    fun `should throw exception when deleting post with unauthorized user`() {
        // Given
        val postId = PostId.generate()
        val authorId = UserId.generate()
        val unauthorizedUserId = UserId.generate()
        val post = createTestPost(postId, "Test Post", "Test Content", authorId)
        
        every { postRepository.findById(postId) } returns post

        // When/Then
        assertThrows<UnauthorizedPostModificationException> {
            postService.deletePost(postId, unauthorizedUserId)
        }
        
        verify { postRepository.findById(postId) }
        verify(exactly = 0) { postRepository.delete(any()) }
    }

    @Test
    fun `should throw exception when post not found for deletion`() {
        // Given
        val postId = PostId.generate()
        val authorId = UserId.generate()
        
        every { postRepository.findById(postId) } returns null

        // When/Then
        assertThrows<PostNotFoundException> {
            postService.deletePost(postId, authorId)
        }
        
        verify { postRepository.findById(postId) }
        verify(exactly = 0) { postRepository.delete(any()) }
    }

    @Test
    fun `should get posts by author`() {
        // Given
        val authorId = UserId.generate()
        val posts = listOf(
            createTestPost(PostId.generate(), "Post 1", "Content 1", authorId),
            createTestPost(PostId.generate(), "Post 2", "Content 2", authorId)
        )
        
        every { userRepository.findById(authorId) } returns mockk()
        every { postRepository.findByAuthorId(authorId) } returns posts

        // When
        val result = postService.getPostsByAuthor(authorId)

        // Then
        assertNotNull(result)
        assertEquals(posts, result)
        
        verify { userRepository.findById(authorId) }
        verify { postRepository.findByAuthorId(authorId) }
    }

    @Test
    fun `should attach image to post successfully`() {
        // Given
        val postId = PostId.generate()
        val authorId = UserId.generate()
        val filename = "test-image.jpg"
        val storagePath = "path/to/test-image.jpg"
        val imageRequest = createTestImageUploadRequest(filename)
        val post = createTestPost(postId, "Test Post", "Test Content", authorId)
        
        every { postRepository.findById(postId) } returns post
        every { imageStorageService.storeImage(any(), any(), any()) } returns storagePath
        every { postRepository.save(any()) } returnsArgument 0

        // When
        val result = postService.attachImageToPost(postId, imageRequest)

        // Then
        assertNotNull(result)
        assertEquals(1, result.imageAttachments.size)
        assertEquals(filename, result.imageAttachments[0].filename)
        assertEquals(storagePath, result.imageAttachments[0].storagePath)
        
        verify { postRepository.findById(postId) }
        verify { imageStorageService.storeImage(any(), any(), any()) }
        verify { postRepository.save(any()) }
    }

    private fun createTestPost(id: PostId, title: String, content: String, authorId: UserId, categoryId: CategoryId = CategoryId.generate()): Post {
        val now = LocalDateTime.now()
        return Post(
            id = id,
            title = title,
            content = content,
            authorId = authorId,
            categoryId = categoryId,
            createdAt = now,
            updatedAt = now,
            imageAttachments = emptyList(),
            likeCount = 0
        )
    }

    private fun createTestImageUploadRequest(filename: String): ImageUploadRequest {
        return ImageUploadRequest(
            filename = filename,
            contentType = "image/jpeg",
            inputStream = ByteArrayInputStream("test image data".toByteArray()),
            fileSizeBytes = 1024L
        )
    }
}

