package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.category.CategoryService
import com.example.vibecoding.application.post.PostService
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for user management operations
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val postService: PostService,
    private val categoryService: CategoryService
) {

    /**
     * Get all users
     */
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserSummaryResponse>> {
        val users = userService.getAllUsers()
        val response = users.map { UserSummaryResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: String): ResponseEntity<UserResponse> {
        val userId = UserId.from(id)
        val user = userService.getUserById(userId)
        val response = UserResponse.from(user)
        return ResponseEntity.ok(response)
    }

    /**
     * Create a new user
     */
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val user = userService.createUser(
            username = request.username,
            email = request.email,
            displayName = request.displayName,
            bio = request.bio
        )
        val response = UserResponse.from(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Update an existing user
     */
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val userId = UserId.from(id)
        var user = userService.getUserById(userId)

        request.displayName?.let { newDisplayName ->
            user = userService.updateUserDisplayName(userId, newDisplayName)
        }

        request.email?.let { newEmail ->
            user = userService.updateUserEmail(userId, newEmail)
        }

        request.bio?.let { newBio ->
            user = userService.updateUserBio(userId, newBio)
        }

        val response = UserResponse.from(user)
        return ResponseEntity.ok(response)
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<Void> {
        val userId = UserId.from(id)
        userService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Get user by username
     */
    @GetMapping("/search")
    fun getUserByUsername(@RequestParam username: String): ResponseEntity<UserResponse> {
        val user = userService.getUserByUsername(username)
        val response = UserResponse.from(user)
        return ResponseEntity.ok(response)
    }

    /**
     * Get posts by user
     */
    @GetMapping("/{id}/posts")
    fun getPostsByUser(@PathVariable id: String): ResponseEntity<List<PostSummaryResponse>> {
        val userId = UserId.from(id)
        val posts = postService.getPostsByAuthor(userId)
        val user = userService.getUserById(userId)
        
        val response = posts.map { post ->
            val category = categoryService.getCategoryById(post.categoryId)
            PostSummaryResponse(
                id = post.id.value.toString(),
                title = post.title,
                author = UserSummaryResponse.from(user),
                category = CategorySummaryResponse.from(category),
                imageCount = post.getImageAttachmentCount(),
                likeCount = post.likeCount,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            )
        }
        
        return ResponseEntity.ok(response)
    }

    /**
     * Check if username is available
     */
    @GetMapping("/check-username")
    fun checkUsernameAvailability(@RequestParam username: String): ResponseEntity<Map<String, Boolean>> {
        val isAvailable = userService.isUsernameAvailable(username)
        return ResponseEntity.ok(mapOf("available" to isAvailable))
    }

    /**
     * Check if email is available
     */
    @GetMapping("/check-email")
    fun checkEmailAvailability(@RequestParam email: String): ResponseEntity<Map<String, Boolean>> {
        val isAvailable = userService.isEmailAvailable(email)
        return ResponseEntity.ok(mapOf("available" to isAvailable))
    }
}
