package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentNotFoundException
import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.comment.CommentId
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class SimpleCommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var commentService: CommentService

    @MockBean
    private lateinit var userService: UserService

    @Test
    fun `should return 404 for non-existent comment`() {
        // Given
        val nonExistentId = "non-existent-id"
        
        // Mock the service to throw CommentNotFoundException
        `when`(commentService.getComment(CommentId.from(nonExistentId)))
            .thenThrow(CommentNotFoundException("Comment with id '$nonExistentId' not found"))
        
        // When & Then
        mockMvc.perform(get("/api/comments/$nonExistentId"))
            .andExpect(status().isNotFound)
    }
}
