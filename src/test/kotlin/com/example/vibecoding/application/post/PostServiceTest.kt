package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PostServiceTest {

    private lateinit var postRepository: PostRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var userRepository: UserRepository
    private lateinit var postService: PostService

    @BeforeEach
    fun setUp() {
        postRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        postService = PostService(postRepository, categoryRepository, userRepository)
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
            bio = null,
            createdAt = now,
            updatedAt = now
        )
    }
}

