package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.*
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import com.example.vibecoding.domain.comment.CommentRepository
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SimplePostServiceTest {

    private lateinit var postRepository: PostRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var userRepository: UserRepository
    private lateinit var imageStorageService: ImageStorageService
    private lateinit var likeRepository: LikeRepository
    private lateinit var commentRepository: CommentRepository
    private lateinit var postService: PostService

    @BeforeEach
    fun setUp() {
        postRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        imageStorageService = mockk(relaxed = true)
        likeRepository = mockk(relaxed = true)
        commentRepository = mockk(relaxed = true)
        
        postService = PostService(
            postRepository, 
            categoryRepository, 
            userRepository, 
            imageStorageService, 
            likeRepository, 
            commentRepository
        )
    }

    @Test
    fun `should delete post successfully`() {
        // Given
        val postId = PostId.generate()
        
        every { postRepository.existsById(postId) } returns true
        every { postRepository.delete(postId) } returns true
        every { likeRepository.deleteByPostId(postId) } returns 5 // 5 likes deleted
        every { commentRepository.deleteByPostId(postId) } returns 3 // 3 comments deleted

        // When
        postService.deletePost(postId)

        // Then
        verify { postRepository.existsById(postId) }
        verify { likeRepository.deleteByPostId(postId) }
        verify { commentRepository.deleteByPostId(postId) }
        verify { postRepository.delete(postId) }
    }
    
    @Test
    fun `should throw exception when post not found for deletion`() {
        // Given
        val postId = PostId.generate()
        
        every { postRepository.existsById(postId) } returns false
        
        // When/Then
        shouldThrow<PostNotFoundException> {
            postService.deletePost(postId)
        }
    }
}

