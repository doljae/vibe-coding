package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.application.user.UserService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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
        mockMvc.perform(get("/api/comments/non-existent-id"))
            .andExpect(status().isNotFound)
    }
}

