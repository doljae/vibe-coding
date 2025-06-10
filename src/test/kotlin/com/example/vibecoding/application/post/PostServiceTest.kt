package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.*
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.LocalDateTime

class PostServiceTest {

    private lateinit var postService: PostService
    private lateinit var postRepository: PostRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var userRepository: UserRepository
    private lateinit var imageStorageService: ImageStorageService

    private lateinit var testUser: User
    private lateinit var testCategory: Category

    @BeforeEach
    fun setUp() {
        postRepository = mockk()
        categoryRepository = mockk()
        userRepository = mockk()
        imageStorageService = mockk()
        
        postService = PostService(postRepository, categoryRepository, userRepository, imageStorageService)

        testUser = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = "Test bio",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        testCategory = Category(
            id = CategoryId.generate(),
            name = "Test Category",
            description = "Test Description",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `should create post successfully`() {
        // Given
        val title = "My First Post"
        val content = "This is the content"
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        val user = createTestUser(authorId, "testuser", "test@example.com")
        
        every { userRepository.findById(authorId) } returns user
        every { categoryRepository.existsById(categoryId) } returns true
        every { postRepository.save(any()) } returnsArgument 0

        // When
        val result = postService.createPost(title, content, authorId, categoryId)

        // Then
        result.title shouldBe title
        result.content shouldBe content
        result.authorId shouldBe authorId
        result.categoryId shouldBe categoryId
        
        verify { userRepository.findById(authorId) }
        verify { categoryRepository.existsById(categoryId) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating post with non-existent user`() {
        // Given
        val title = "My First Post"
        val content = "This is the content"
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        
        every { userRepository.findById(authorId) } returns null

        // When & Then
        shouldThrow<UserNotFoundException> {
            postService.createPost(title, content, authorId, categoryId)
        }
        
        verify { userRepository.findById(authorId) }
        verify(exactly = 0) { postRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating post with non-existent category`() {
        // Given
        val title = "My First Post"
        val content = "This is the content"
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        val user = createTestUser(authorId, "testuser", "test@example.com")
        
        every { userRepository.findById(authorId) } returns user
        every { categoryRepository.existsById(categoryId) } returns false

        // When & Then
        shouldThrow<CategoryNotFoundException> {
            postService.createPost(title, content, authorId, categoryId)
        }
        
        verify { userRepository.findById(authorId) }
        verify { categoryRepository.existsById(categoryId) }
        verify(exactly = 0) { postRepository.save(any()) }
    }

    @Test
    fun `should update post successfully`() {
        // Given
        val postId = PostId.generate()
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        val existingPost = createTestPost(postId, "Old Title", "Old Content", authorId, categoryId)
        val newTitle = "New Title"
        val newContent = "New Content"
        val newCategoryId = CategoryId.generate()
        
        every { postRepository.findById(postId) } returns existingPost
        every { categoryRepository.existsById(newCategoryId) } returns true
        every { postRepository.save(any()) } returnsArgument 0

        // When
        val result = postService.updatePost(postId, newTitle, newContent, newCategoryId)

        // Then
        result.title shouldBe newTitle
        result.content shouldBe newContent
        result.categoryId shouldBe newCategoryId
        
        verify { postRepository.findById(postId) }
        verify { categoryRepository.existsById(newCategoryId) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should throw exception when updating non-existent post`() {
        // Given
        val postId = PostId.generate()
        
        every { postRepository.findById(postId) } returns null

        // When & Then
        shouldThrow<PostNotFoundException> {
            postService.updatePost(postId, "New Title", "New Content", null)
        }
        
        verify { postRepository.findById(postId) }
        verify(exactly = 0) { postRepository.save(any()) }
    }

    @Test
    fun `should get posts by author successfully`() {
        // Given
        val authorId = UserId.generate()
        val user = createTestUser(authorId, "testuser", "test@example.com")
        val posts = listOf(
            createTestPost(PostId.generate(), "Post 1", "Content 1", authorId, CategoryId.generate()),
            createTestPost(PostId.generate(), "Post 2", "Content 2", authorId, CategoryId.generate())
        )
        
        every { userRepository.findById(authorId) } returns user
        every { postRepository.findByAuthorId(authorId) } returns posts

        // When
        val result = postService.getPostsByAuthor(authorId)

        // Then
        result shouldBe posts
        
        verify { userRepository.findById(authorId) }
        verify { postRepository.findByAuthorId(authorId) }
    }

    @Test
    fun `should throw exception when getting posts by non-existent author`() {
        // Given
        val authorId = UserId.generate()
        
        every { userRepository.findById(authorId) } returns null

        // When & Then
        shouldThrow<UserNotFoundException> {
            postService.getPostsByAuthor(authorId)
        }
        
        verify { userRepository.findById(authorId) }
    }

    @Test
    fun `should get post count by author successfully`() {
        // Given
        val authorId = UserId.generate()
        val count = 5L
        
        every { postRepository.countByAuthorId(authorId) } returns count

        // When
        val result = postService.getPostCountByAuthor(authorId)

        // Then
        result shouldBe count
        
        verify { postRepository.countByAuthorId(authorId) }
    }

    @Test
    fun `should get post by id successfully`() {
        // Given
        val postId = PostId.generate()
        val post = createTestPost(postId, "Title", "Content", UserId.generate(), CategoryId.generate())
        
        every { postRepository.findById(postId) } returns post

        // When
        val result = postService.getPostById(postId)

        // Then
        result shouldBe post
        
        verify { postRepository.findById(postId) }
    }

    @Test
    fun `should throw exception when getting non-existent post`() {
        // Given
        val postId = PostId.generate()
        
        every { postRepository.findById(postId) } returns null

        // When & Then
        shouldThrow<PostNotFoundException> {
            postService.getPostById(postId)
        }
        
        verify { postRepository.findById(postId) }
    }

    @Test
    fun `should get all posts successfully`() {
        // Given
        val posts = listOf(
            createTestPost(PostId.generate(), "Post 1", "Content 1", UserId.generate(), CategoryId.generate()),
            createTestPost(PostId.generate(), "Post 2", "Content 2", UserId.generate(), CategoryId.generate())
        )
        
        every { postRepository.findAll() } returns posts

        // When
        val result = postService.getAllPosts()

        // Then
        result shouldBe posts
        
        verify { postRepository.findAll() }
    }

    @Test
    fun `should get posts by category successfully`() {
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
        result shouldBe posts
        
        verify { categoryRepository.existsById(categoryId) }
        verify { postRepository.findByCategoryId(categoryId) }
    }

    @Test
    fun `should search posts by title successfully`() {
        // Given
        val title = "Technology"
        val posts = listOf(
            createTestPost(PostId.generate(), "Technology Post", "Content", UserId.generate(), CategoryId.generate())
        )
        
        every { postRepository.findByTitle(title) } returns posts

        // When
        val result = postService.searchPostsByTitle(title)

        // Then
        result shouldBe posts
        
        verify { postRepository.findByTitle(title) }
    }

    @Test
    fun `should delete post successfully`() {
        // Given
        val postId = PostId.generate()
        
        every { postRepository.existsById(postId) } returns true
        every { postRepository.delete(postId) } returns true

        // When
        postService.deletePost(postId)

        // Then
        verify { postRepository.existsById(postId) }
        verify { postRepository.delete(postId) }
    }

    @Test
    fun `should throw exception when deleting non-existent post`() {
        // Given
        val postId = PostId.generate()
        
        every { postRepository.existsById(postId) } returns false

        // When & Then
        shouldThrow<PostNotFoundException> {
            postService.deletePost(postId)
        }
        
        verify { postRepository.existsById(postId) }
    }

    @Test
    fun `should create post with images successfully`() {
        // Given
        val title = "Test Post"
        val content = "Test Content"
        val imageRequest1 = createTestImageUploadRequest("image1.jpg")
        val imageRequest2 = createTestImageUploadRequest("image2.jpg")
        val images = listOf(imageRequest1, imageRequest2)

        every { userRepository.findById(testUser.id) } returns testUser
        every { categoryRepository.existsById(testCategory.id) } returns true
        every { imageStorageService.storeImage(any(), any(), any()) } returnsMany listOf("path1.jpg", "path2.jpg")
        every { postRepository.save(any()) } returnsArgument 0

        // When
        val result = postService.createPostWithImages(title, content, testUser.id, testCategory.id, images)

        // Then
        result.title shouldBe title
        result.content shouldBe content
        result.authorId shouldBe testUser.id
        result.categoryId shouldBe testCategory.id
        result.imageAttachments.size shouldBe 2
        
        verify { imageStorageService.storeImage("image1.jpg", "image/jpeg", any()) }
        verify { imageStorageService.storeImage("image2.jpg", "image/jpeg", any()) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating post with too many images`() {
        // Given
        val title = "Test Post"
        val content = "Test Content"
        val images = listOf(
            createTestImageUploadRequest("image1.jpg"),
            createTestImageUploadRequest("image2.jpg"),
            createTestImageUploadRequest("image3.jpg"),
            createTestImageUploadRequest("image4.jpg") // One too many
        )

        every { userRepository.findById(testUser.id) } returns testUser
        every { categoryRepository.existsById(testCategory.id) } returns true

        // When & Then
        shouldThrow<IllegalArgumentException> {
            postService.createPostWithImages(title, content, testUser.id, testCategory.id, images)
        }
    }

    @Test
    fun `should attach image to existing post successfully`() {
        // Given
        val existingPost = createTestPost()
        val imageRequest = createTestImageUploadRequest("new-image.jpg")
        val storagePath = "path/to/new-image.jpg"

        every { postRepository.findById(existingPost.id) } returns existingPost
        every { imageStorageService.storeImage("new-image.jpg", "image/jpeg", any()) } returns storagePath
        every { postRepository.save(any()) } returnsArgument 0

        // When
        val result = postService.attachImageToPost(existingPost.id, imageRequest)

        // Then
        result.imageAttachments.size shouldBe 1
        result.imageAttachments[0].filename shouldBe "new-image.jpg"
        result.imageAttachments[0].storagePath shouldBe storagePath
        
        verify { imageStorageService.storeImage("new-image.jpg", "image/jpeg", any()) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should throw exception when attaching image to post with maximum images`() {
        // Given
        val postWithMaxImages = createTestPostWithMaxImages()
        val imageRequest = createTestImageUploadRequest("extra-image.jpg")

        every { postRepository.findById(postWithMaxImages.id) } returns postWithMaxImages

        // When & Then
        shouldThrow<ImageAttachmentException> {
            postService.attachImageToPost(postWithMaxImages.id, imageRequest)
        }
    }

    @Test
    fun `should remove image from post successfully`() {
        // Given
        val imageAttachment = createTestImageAttachment("test-image.jpg")
        val postWithImage = createTestPost().addImageAttachment(imageAttachment)

        every { postRepository.findById(postWithImage.id) } returns postWithImage
        every { imageStorageService.deleteImage(imageAttachment.storagePath) } just Runs
        every { postRepository.save(any()) } returnsArgument 0

        // When
        val result = postService.removeImageFromPost(postWithImage.id, imageAttachment.id)

        // Then
        result.imageAttachments.size shouldBe 0
        
        verify { imageStorageService.deleteImage(imageAttachment.storagePath) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should continue removing image from post even if storage deletion fails`() {
        // Given
        val imageAttachment = createTestImageAttachment("test-image.jpg")
        val postWithImage = createTestPost().addImageAttachment(imageAttachment)

        every { postRepository.findById(postWithImage.id) } returns postWithImage
        every { imageStorageService.deleteImage(imageAttachment.storagePath) } throws ImageStorageException("Storage error")
        every { postRepository.save(any()) } returnsArgument 0

        // When
        val result = postService.removeImageFromPost(postWithImage.id, imageAttachment.id)

        // Then
        result.imageAttachments.size shouldBe 0
        
        verify { imageStorageService.deleteImage(imageAttachment.storagePath) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should throw exception when removing non-existent image`() {
        // Given
        val post = createTestPost()
        val nonExistentImageId = ImageId.generate()

        every { postRepository.findById(post.id) } returns post

        // When & Then
        shouldThrow<ImageAttachmentException> {
            postService.removeImageFromPost(post.id, nonExistentImageId)
        }
    }

    @Test
    fun `should get post image successfully`() {
        // Given
        val imageAttachment = createTestImageAttachment("test-image.jpg")
        val postWithImage = createTestPost().addImageAttachment(imageAttachment)

        every { postRepository.findById(postWithImage.id) } returns postWithImage

        // When
        val result = postService.getPostImage(postWithImage.id, imageAttachment.id)

        // Then
        result shouldBe imageAttachment
    }

    @Test
    fun `should get post image data successfully`() {
        // Given
        val imageAttachment = createTestImageAttachment("test-image.jpg")
        val postWithImage = createTestPost().addImageAttachment(imageAttachment)
        val imageData = ByteArrayInputStream("image data".toByteArray())

        every { postRepository.findById(postWithImage.id) } returns postWithImage
        every { imageStorageService.retrieveImage(imageAttachment.storagePath) } returns imageData

        // When
        val result = postService.getPostImageData(postWithImage.id, imageAttachment.id)

        // Then
        result shouldBe imageData
        
        verify { imageStorageService.retrieveImage(imageAttachment.storagePath) }
    }

    @Test
    fun `should throw exception when storage fails during image attachment`() {
        // Given
        val existingPost = createTestPost()
        val imageRequest = createTestImageUploadRequest("failing-image.jpg")

        every { postRepository.findById(existingPost.id) } returns existingPost
        every { imageStorageService.storeImage(any(), any(), any()) } throws ImageStorageException("Storage failed")

        // When & Then
        shouldThrow<ImageAttachmentException> {
            postService.attachImageToPost(existingPost.id, imageRequest)
        }
    }

    private fun createTestPost(id: PostId, title: String, content: String, authorId: UserId, categoryId: CategoryId): Post {
        val now = LocalDateTime.now()
        return Post(
            id = id,
            title = title,
            content = content,
            authorId = authorId,
            categoryId = categoryId,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createTestUser(id: UserId, username: String, email: String): User {
        val now = LocalDateTime.now()
        return User(
            id = id,
            username = username,
            email = email,
            displayName = "Test User",
            bio = "Test bio",
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createTestImageUploadRequest(filename: String): ImageUploadRequest {
        return ImageUploadRequest(
            filename = filename,
            contentType = "image/jpeg",
            fileSizeBytes = 1024L,
            inputStream = ByteArrayInputStream("fake image data".toByteArray())
        )
    }

    private fun createTestImageAttachment(filename: String): ImageAttachment {
        return ImageAttachment.create(
            filename = filename,
            storagePath = "test/path/$filename",
            contentType = "image/jpeg",
            fileSizeBytes = 1024L
        )
    }

    private fun createTestPost(): Post {
        val now = LocalDateTime.now()
        return Post(
            id = PostId.generate(),
            title = "Test Post",
            content = "Test Content",
            authorId = testUser.id,
            categoryId = testCategory.id,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createTestPostWithMaxImages(): Post {
        val post = createTestPost()
        val image1 = createTestImageAttachment("image1.jpg")
        val image2 = createTestImageAttachment("image2.jpg")
        val image3 = createTestImageAttachment("image3.jpg")
        
        return post
            .addImageAttachment(image1)
            .addImageAttachment(image2)
            .addImageAttachment(image3)
    }
}
