package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.application.comment.CommentWithReplies
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.exception.GlobalExceptionHandler
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

@WebMvcTest(CommentController::class)
class CommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var commentService: CommentService

    @MockkBean
    private lateinit var userService: UserService

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

