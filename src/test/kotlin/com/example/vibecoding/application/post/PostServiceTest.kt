package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDateTime
import kotlin.test.assertEquals

class PostServiceTest {

    private lateinit var postRepository: PostRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var postService: PostService

    @BeforeEach
    fun setUp() {
        postRepository = mock()
        categoryRepository = mock()
        postService = PostService(postRepository, categoryRepository)
    }

    @Test
    fun `should create post successfully`() {
        // Given
        val title = "My First Post"
        val content = "This is the content"
        val categoryId = CategoryId.generate()
        
        whenever(categoryRepository.existsById(categoryId)).thenReturn(true)
        whenever(postRepository.save(any())).thenAnswer { it.arguments[0] as Post }

        // When
        val result = postService.createPost(title, content, categoryId)

        // Then
        assertEquals(title, result.title)
        assertEquals(content, result.content)
        assertEquals(categoryId, result.categoryId)
    }

    @Test
    fun `should throw exception when creating post with non-existent category`() {
        // Given
        val title = "My First Post"
        val content = "This is the content"
        val categoryId = CategoryId.generate()
        
        whenever(categoryRepository.existsById(categoryId)).thenReturn(false)

        // When & Then
        assertThrows<CategoryNotFoundException> {
            postService.createPost(title, content, categoryId)
        }
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
        
        whenever(postRepository.findById(postId)).thenReturn(existingPost)
        whenever(categoryRepository.existsById(newCategoryId)).thenReturn(true)
        whenever(postRepository.save(any())).thenAnswer { it.arguments[0] as Post }

        // When
        val result = postService.updatePost(postId, newTitle, newContent, newCategoryId)

        // Then
        assertEquals(newTitle, result.title)
        assertEquals(newContent, result.content)
        assertEquals(newCategoryId, result.categoryId)
    }

    @Test
    fun `should throw exception when updating non-existent post`() {
        // Given
        val postId = PostId.generate()
        
        whenever(postRepository.findById(postId)).thenReturn(null)

        // When & Then
        assertThrows<PostNotFoundException> {
            postService.updatePost(postId, "New Title", "New Content", null)
        }
    }

    @Test
    fun `should get post by id successfully`() {
        // Given
        val postId = PostId.generate()
        val post = createTestPost(postId, "Title", "Content", CategoryId.generate())
        
        whenever(postRepository.findById(postId)).thenReturn(post)

        // When
        val result = postService.getPostById(postId)

        // Then
        assertEquals(post, result)
    }

    @Test
    fun `should throw exception when getting non-existent post`() {
        // Given
        val postId = PostId.generate()
        
        whenever(postRepository.findById(postId)).thenReturn(null)

        // When & Then
        assertThrows<PostNotFoundException> {
            postService.getPostById(postId)
        }
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

