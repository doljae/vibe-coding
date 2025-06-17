package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.application.comment.CommentWithReplies
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import com.example.vibecoding.presentation.exception.GlobalExceptionHandler
import java.time.LocalDateTime

class CommentControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var commentService: CommentService
    private lateinit var userService: UserService
    private lateinit var objectMapper: ObjectMapper

    private val userId = UserId.generate()
    private val postId = PostId.generate()
    private val commentId = CommentId.generate()
    private val validContent = "This is a valid comment"
    
    @BeforeEach
    fun setUp() {
        commentService = mockk(relaxed = true)
        userService = mockk(relaxed = true)
        objectMapper = ObjectMapper()
        
        val controller = CommentController(commentService, userService)
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }
}

