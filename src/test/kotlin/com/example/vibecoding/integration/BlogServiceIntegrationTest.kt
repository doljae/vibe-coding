package com.example.vibecoding.integration

import com.example.vibecoding.presentation.dto.CreateCategoryRequest
import com.example.vibecoding.presentation.dto.CreatePostRequest
import com.example.vibecoding.presentation.dto.CreateUserRequest
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

/**
 * Integration tests for the complete blog service API
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BlogServiceIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun createUrl(path: String) = "http://localhost:$port$path"

    @Test
    fun `complete blog workflow should work end-to-end`() {
        // 1. Create a category
        val categoryRequest = CreateCategoryRequest(
            name = "Technology",
            description = "Technology related posts"
        )
        
        val categoryResult = restTemplate.postForEntity(
            createUrl("/api/categories"),
            categoryRequest,
            String::class.java
        )
        
        categoryResult.statusCode shouldBe HttpStatus.CREATED
        categoryResult.body.shouldNotBeNull()
        
        val categoryResponse = objectMapper.readTree(categoryResult.body)
        val categoryId = categoryResponse.get("id").asText()
        categoryResponse.get("name").asText() shouldBe "Technology"

        // 2. Create a user
        val userRequest = CreateUserRequest(
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = "Test bio"
        )
        
        val userResult = restTemplate.postForEntity(
            createUrl("/api/users"),
            userRequest,
            String::class.java
        )
        
        userResult.statusCode shouldBe HttpStatus.CREATED
        userResult.body.shouldNotBeNull()
        
        val userResponse = objectMapper.readTree(userResult.body)
        val userId = userResponse.get("id").asText()
        userResponse.get("username").asText() shouldBe "testuser"

        // 3. Create a post
        val postRequest = CreatePostRequest(
            title = "My First Post",
            content = "This is the content of my first post",
            authorId = userId,
            categoryId = categoryId
        )
        
        val postResult = restTemplate.postForEntity(
            createUrl("/api/posts"),
            postRequest,
            String::class.java
        )
        
        postResult.statusCode shouldBe HttpStatus.CREATED
        postResult.body.shouldNotBeNull()
        
        val postResponse = objectMapper.readTree(postResult.body)
        val postId = postResponse.get("id").asText()
        postResponse.get("title").asText() shouldBe "My First Post"

        // 4. Get all posts and verify our post is there
        val allPostsResult = restTemplate.getForEntity(
            createUrl("/api/posts"),
            String::class.java
        )
        
        allPostsResult.statusCode shouldBe HttpStatus.OK
        allPostsResult.body.shouldNotBeNull()
        
        val allPostsResponse = objectMapper.readTree(allPostsResult.body)
        allPostsResponse.size() shouldBe 1
        allPostsResponse[0].get("id").asText() shouldBe postId

        // 5. Get specific post
        val specificPostResult = restTemplate.getForEntity(
            createUrl("/api/posts/$postId"),
            String::class.java
        )
        
        specificPostResult.statusCode shouldBe HttpStatus.OK
        specificPostResult.body.shouldNotBeNull()
        
        val specificPostResponse = objectMapper.readTree(specificPostResult.body)
        specificPostResponse.get("id").asText() shouldBe postId
        specificPostResponse.get("title").asText() shouldBe "My First Post"
        specificPostResponse.get("content").asText() shouldBe "This is the content of my first post"

        // 6. Get specific user
        val specificUserResult = restTemplate.getForEntity(
            createUrl("/api/users/$userId"),
            String::class.java
        )
        
        specificUserResult.statusCode shouldBe HttpStatus.OK
        specificUserResult.body.shouldNotBeNull()
        
        val specificUserResponse = objectMapper.readTree(specificUserResult.body)
        specificUserResponse.get("id").asText() shouldBe userId
        specificUserResponse.get("username").asText() shouldBe "testuser"

        // 7. Get specific category
        val specificCategoryResult = restTemplate.getForEntity(
            createUrl("/api/categories/$categoryId"),
            String::class.java
        )
        
        specificCategoryResult.statusCode shouldBe HttpStatus.OK
        specificCategoryResult.body.shouldNotBeNull()
        
        val specificCategoryResponse = objectMapper.readTree(specificCategoryResult.body)
        specificCategoryResponse.get("id").asText() shouldBe categoryId
        specificCategoryResponse.get("name").asText() shouldBe "Technology"
    }

    @Test
    fun `should handle validation errors properly`() {
        // Test invalid category creation
        val invalidCategoryRequest = CreateCategoryRequest(
            name = "", // Invalid: blank name
            description = "Test description"
        )
        
        val categoryResult = restTemplate.postForEntity(
            createUrl("/api/categories"),
            invalidCategoryRequest,
            String::class.java
        )
        
        categoryResult.statusCode shouldBe HttpStatus.BAD_REQUEST

        // Test invalid user creation
        val invalidUserRequest = CreateUserRequest(
            username = "ab", // Invalid: too short
            email = "invalid-email", // Invalid: bad format
            displayName = "Test User"
        )
        
        val userResult = restTemplate.postForEntity(
            createUrl("/api/users"),
            invalidUserRequest,
            String::class.java
        )
        
        userResult.statusCode shouldBe HttpStatus.BAD_REQUEST
    }

    @Test
    fun `should handle not found errors properly`() {
        val nonExistentId = "00000000-0000-0000-0000-000000000000"
        
        // Test category not found
        val categoryResult = restTemplate.getForEntity(
            createUrl("/api/categories/$nonExistentId"),
            String::class.java
        )
        categoryResult.statusCode shouldBe HttpStatus.NOT_FOUND

        // Test post not found
        val postResult = restTemplate.getForEntity(
            createUrl("/api/posts/$nonExistentId"),
            String::class.java
        )
        postResult.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    @Test
    fun `should handle username and email availability checks`() {
        // Check available username
        val usernameResult = restTemplate.getForEntity(
            createUrl("/api/users/check-username?username=availableuser"),
            String::class.java
        )
        
        usernameResult.statusCode shouldBe HttpStatus.OK
        usernameResult.body.shouldNotBeNull()
        
        val usernameResponse = objectMapper.readTree(usernameResult.body)
        usernameResponse.get("available").asBoolean() shouldBe true

        // Check available email
        val emailResult = restTemplate.getForEntity(
            createUrl("/api/users/check-email?email=available@example.com"),
            String::class.java
        )
        
        emailResult.statusCode shouldBe HttpStatus.OK
        emailResult.body.shouldNotBeNull()
        
        val emailResponse = objectMapper.readTree(emailResult.body)
        emailResponse.get("available").asBoolean() shouldBe true
    }
}
