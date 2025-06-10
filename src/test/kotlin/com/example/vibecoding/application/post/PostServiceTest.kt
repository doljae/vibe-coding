package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PostServiceTest {

    private lateinit var postRepository: PostRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var postService: PostService

    @BeforeEach
    fun setUp() {
        postRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        postService = PostService(postRepository, categoryRepository)
    }

    @Test
    fun `should create post successfully`() {
        // Given
        val title = "My First Post"
        val content = "This is the content"
        val categoryId = CategoryId.generate()
        
        every { categoryRepository.existsById(categoryId) } returns true
        every { postRepository.save(any()) } returnsArgument 0

        // When
        val result = postService.createPost(title, content, categoryId)

        // Then
        result.title shouldBe title
        result.content shouldBe content
        result.categoryId shouldBe categoryId
        
        verify { categoryRepository.existsById(categoryId) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating post with non-existent category`() {
        // Given
        val title = "My First Post"
        val content = "This is the content"
        val categoryId = CategoryId.generate()
        
        every { categoryRepository.existsById(categoryId) } returns false

        // When & Then
        shouldThrow<CategoryNotFoundException> {
            postService.createPost(title, content, categoryId)
        }
        
        verify { categoryRepository.existsById(categoryId) }
        verify(exactly = 0) { postRepository.save(any()) }
    }

    @Test
    fun `should update post successfully`() {
        // Given
        val postId = PostId.generate()
        val categoryId = CategoryId.generate()
        val existingPost = createTestPost(postId, "Old Title", "Old Content", categoryId)
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
    fun `should throw exception when updating with non-existent category`() {
        // Given
        val postId = PostId.generate()
        val categoryId = CategoryId.generate()
        val existingPost = createTestPost(postId, "Title", "Content", categoryId)
        val newCategoryId = CategoryId.generate()
        
        every { postRepository.findById(postId) } returns existingPost
        every { categoryRepository.existsById(newCategoryId) } returns false

        // When & Then
        shouldThrow<CategoryNotFoundException> {
            postService.updatePost(postId, null, null, newCategoryId)
        }
        
        verify { postRepository.findById(postId) }
        verify { categoryRepository.existsById(newCategoryId) }
        verify(exactly = 0) { postRepository.save(any()) }
    }

    @Test
    fun `should get post by id successfully`() {
        // Given
        val postId = PostId.generate()
        val post = createTestPost(postId, "Title", "Content", CategoryId.generate())
        
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
            createTestPost(PostId.generate(), "Post 1", "Content 1", CategoryId.generate()),
            createTestPost(PostId.generate(), "Post 2", "Content 2", CategoryId.generate())
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
            createTestPost(PostId.generate(), "Post 1", "Content 1", categoryId),
            createTestPost(PostId.generate(), "Post 2", "Content 2", categoryId)
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
    fun `should throw exception when getting posts by non-existent category`() {
        // Given
        val categoryId = CategoryId.generate()
        
        every { categoryRepository.existsById(categoryId) } returns false

        // When & Then
        shouldThrow<CategoryNotFoundException> {
            postService.getPostsByCategory(categoryId)
        }
        
        verify { categoryRepository.existsById(categoryId) }
    }

    @Test
    fun `should search posts by title successfully`() {
        // Given
        val title = "Technology"
        val posts = listOf(
            createTestPost(PostId.generate(), "Technology Post", "Content", CategoryId.generate())
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

    private fun createTestPost(id: PostId, title: String, content: String, categoryId: CategoryId): Post {
        val now = LocalDateTime.now()
        return Post(
            id = id,
            title = title,
            content = content,
            categoryId = categoryId,
            createdAt = now,
            updatedAt = now
        )
    }
}

