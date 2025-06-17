package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.comment.CommentService
import com.example.vibecoding.application.comment.CommentWithReplies
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime
import java.util.*

class CommentControllerStandaloneTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var commentService: TestCommentService
    private lateinit var userService: TestUserService
    private lateinit var commentController: CommentController
    private lateinit var objectMapper: ObjectMapper

    private val userId = UserId.generate()
    private val postId = PostId.generate()
    private val commentId = CommentId.generate()
    private val validContent = "This is a valid comment"

    @BeforeEach
    fun setUp() {
        commentService = TestCommentService()
        userService = TestUserService()
        commentController = CommentController(commentService, userService)
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build()
        objectMapper = ObjectMapper()
    }

    @Test
    fun `should get comment successfully`() {
        // Given
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        commentService.addComment(comment)

        // When & Then
        mockMvc.perform(get("/api/comments/{commentId}", commentId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(commentId.value.toString()))
            .andExpect(jsonPath("$.content").value(validContent))
            .andExpect(jsonPath("$.authorId").value(userId.value.toString()))
            .andExpect(jsonPath("$.postId").value(postId.value.toString()))
    }

    @Test
    fun `should get comment count for post successfully`() {
        // Given
        val comment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Comment 1",
            authorId = userId,
            postId = postId
        )
        val comment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Comment 2",
            authorId = userId,
            postId = postId
        )
        commentService.addComment(comment1)
        commentService.addComment(comment2)

        // When & Then
        mockMvc.perform(get("/api/comments/posts/{postId}/count", postId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(2))
    }

    @Test
    fun `should check if comment exists successfully`() {
        // Given
        val comment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        commentService.addComment(comment)

        // When & Then
        mockMvc.perform(get("/api/comments/{commentId}/exists", commentId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.exists").value(true))
    }

    @Test
    fun `should get comments for post successfully`() {
        // Given
        val rootComment = Comment.createRootComment(
            id = commentId,
            content = validContent,
            authorId = userId,
            postId = postId
        )
        val reply = Comment.createReply(
            id = CommentId.generate(),
            content = "Reply content",
            authorId = userId,
            postId = postId,
            parentComment = rootComment
        )
        commentService.addComment(rootComment)
        commentService.addComment(reply)

        // When & Then
        mockMvc.perform(get("/api/comments/posts/{postId}", postId.value.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.postId").value(postId.value.toString()))
            .andExpect(jsonPath("$.totalCommentCount").value(2))
            .andExpect(jsonPath("$.comments").isArray)
            .andExpect(jsonPath("$.comments[0].comment.id").value(commentId.value.toString()))
            .andExpect(jsonPath("$.comments[0].replies").isArray)
            .andExpect(jsonPath("$.comments[0].replyCount").value(1))
    }

    // Test implementation of CommentService
    class TestCommentService : CommentService {
        private val comments = mutableMapOf<CommentId, Comment>()

        fun addComment(comment: Comment) {
            comments[comment.id] = comment
        }

        override fun createComment(content: String, authorId: UserId, postId: PostId): Comment {
            val comment = Comment.createRootComment(
                id = CommentId.generate(),
                content = content,
                authorId = authorId,
                postId = postId
            )
            comments[comment.id] = comment
            return comment
        }

        override fun createReply(content: String, authorId: UserId, postId: PostId, parentCommentId: CommentId): Comment {
            val parentComment = comments[parentCommentId] ?: throw RuntimeException("Parent comment not found")
            val reply = Comment.createReply(
                id = CommentId.generate(),
                content = content,
                authorId = authorId,
                postId = postId,
                parentComment = parentComment
            )
            comments[reply.id] = reply
            return reply
        }

        override fun updateComment(commentId: CommentId, newContent: String, authorId: UserId): Comment {
            val comment = comments[commentId] ?: throw RuntimeException("Comment not found")
            val updatedComment = comment.updateContent(newContent)
            comments[commentId] = updatedComment
            return updatedComment
        }

        override fun deleteComment(commentId: CommentId, authorId: UserId) {
            comments.remove(commentId)
        }

        override fun getComment(commentId: CommentId): Comment {
            return comments[commentId] ?: throw RuntimeException("Comment not found")
        }

        override fun getCommentCountForPost(postId: PostId): Long {
            return comments.values.count { it.postId == postId }.toLong()
        }

        override fun commentExists(commentId: CommentId): Boolean {
            return comments.containsKey(commentId)
        }

        override fun getCommentsForPost(postId: PostId): List<CommentWithReplies> {
            val rootComments = comments.values.filter { it.postId == postId && it.isRootComment() }
            return rootComments.map { rootComment ->
                val replies = comments.values.filter { it.parentCommentId == rootComment.id }
                CommentWithReplies(rootComment, replies)
            }
        }
    }
    
    // Test implementation of UserService
    class TestUserService : UserService {
        private val users = mutableMapOf<UserId, User>()
        
        override fun createUser(username: String, email: String, displayName: String): User {
            val user = User(
                id = UserId.generate(),
                username = username,
                email = email,
                displayName = displayName,
                bio = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            users[user.id] = user
            return user
        }
        
        override fun getUserById(userId: UserId): User {
            return users[userId] ?: throw RuntimeException("User not found")
        }
        
        override fun getUserByUsername(username: String): User {
            return users.values.find { it.username == username } ?: throw RuntimeException("User not found")
        }
        
        override fun updateUser(userId: UserId, displayName: String, bio: String?): User {
            val user = users[userId] ?: throw RuntimeException("User not found")
            val updatedUser = user.copy(
                displayName = displayName,
                bio = bio,
                updatedAt = LocalDateTime.now()
            )
            users[userId] = updatedUser
            return updatedUser
        }
        
        override fun deleteUser(userId: UserId) {
            users.remove(userId)
        }
        
        override fun userExists(userId: UserId): Boolean {
            return users.containsKey(userId)
        }
        
        override fun usernameExists(username: String): Boolean {
            return users.values.any { it.username == username }
        }
    }
}

