package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.post.PostService
import com.example.vibecoding.domain.post.PostId
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class PostDeletionTest {

    @Test
    fun `should delete post successfully`() {
        // Given
        val postService = mock(PostService::class.java)
        val postController = PostController(
            postService = postService,
            userService = mock(),
            categoryService = mock()
        )
        
        val mockMvc = MockMvcBuilders.standaloneSetup(postController).build()
        val postId = "123e4567-e89b-12d3-a456-426614174000"
        val postIdObj = PostId.from(postId)
        
        // Use doNothing() with the exact PostId object instead of any()
        doNothing().`when`(postService).deletePost(postIdObj)
        
        // When & Then
        mockMvc.perform(delete("/api/posts/$postId"))
            .andExpect(status().isNoContent)
        
        verify(postService).deletePost(postIdObj)
    }
}
