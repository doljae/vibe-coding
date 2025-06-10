package com.example.vibecoding.integration

import com.example.vibecoding.presentation.dto.CreateCategoryRequest
import com.example.vibecoding.presentation.dto.CreatePostRequest
import com.example.vibecoding.presentation.dto.CreateUserRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
        
        assertEquals(HttpStatus.CREATED, categoryResult.statusCode)
        assertNotNull(categoryResult.body)
        
        val categoryResponse = objectMapper.readTree(categoryResult.body)
        val categoryId = categoryResponse.get("id").asText()
        assertEquals("Technology", categoryResponse.get("name").asText())

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
        
        assertEquals(HttpStatus.CREATED, userResult.statusCode)
        assertNotNull(userResult.body)
        
        val userResponse = objectMapper.readTree(userResult.body)
        val userId = userResponse.get("id").asText()
        assertEquals("testuser", userResponse.get("username").asText())

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
        
        assertEquals(HttpStatus.CREATED, postResult.statusCode)
        assertNotNull(postResult.body)
        
        val postResponse = objectMapper.readTree(postResult.body)
        val postId = postResponse.get("id").asText()
        assertEquals("My First Post", postResponse.get("title").asText())

        // 4. Get all posts and verify our post is there
        val allPostsResult = restTemplate.getForEntity(
            createUrl("/api/posts"),
            String::class.java
        )
        
        assertEquals(HttpStatus.OK, allPostsResult.statusCode)
        assertNotNull(allPostsResult.body)
        
        val allPostsResponse = objectMapper.readTree(allPostsResult.body)
        assertEquals(1, allPostsResponse.size())
        assertEquals(postId, allPostsResponse[0].get("id").asText())

        // 5. Get specific post
        val specificPostResult = restTemplate.getForEntity(
            createUrl("/api/posts/$postId"),
            String::class.java
        )
        
        assertEquals(HttpStatus.OK, specificPostResult.statusCode)
        assertNotNull(specificPostResult.body)
        
        val specificPostResponse = objectMapper.readTree(specificPostResult.body)
        assertEquals(postId, specificPostResponse.get("id").asText())
        assertEquals("My First Post", specificPostResponse.get("title").asText())
        assertEquals("This is the content of my first post", specificPostResponse.get("content").asText())

        // 6. Get specific user
        val specificUserResult = restTemplate.getForEntity(
            createUrl("/api/users/$userId"),
            String::class.java
        )
        
        assertEquals(HttpStatus.OK, specificUserResult.statusCode)
        assertNotNull(specificUserResult.body)
        
        val specificUserResponse = objectMapper.readTree(specificUserResult.body)
        assertEquals(userId, specificUserResponse.get("id").asText())
        assertEquals("testuser", specificUserResponse.get("username").asText())

        // 7. Get specific category
        val specificCategoryResult = restTemplate.getForEntity(
            createUrl("/api/categories/$categoryId"),
            String::class.java
        )
        
        assertEquals(HttpStatus.OK, specificCategoryResult.statusCode)
        assertNotNull(specificCategoryResult.body)
        
        val specificCategoryResponse = objectMapper.readTree(specificCategoryResult.body)
        assertEquals(categoryId, specificCategoryResponse.get("id").asText())
        assertEquals("Technology", specificCategoryResponse.get("name").asText())
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
        
        assertEquals(HttpStatus.BAD_REQUEST, categoryResult.statusCode)

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
        
        assertEquals(HttpStatus.BAD_REQUEST, userResult.statusCode)
    }

    @Test
    fun `should handle not found errors properly`() {
        val nonExistentId = "00000000-0000-0000-0000-000000000000"
        
        // Test category not found
        val categoryResult = restTemplate.getForEntity(
            createUrl("/api/categories/$nonExistentId"),
            String::class.java
        )
        assertEquals(HttpStatus.NOT_FOUND, categoryResult.statusCode)

        // Test post not found
        val postResult = restTemplate.getForEntity(
            createUrl("/api/posts/$nonExistentId"),
            String::class.java
        )
        assertEquals(HttpStatus.NOT_FOUND, postResult.statusCode)
    }

    @Test
    fun `should handle username and email availability checks`() {
        // Check available username
        val usernameResult = restTemplate.getForEntity(
            createUrl("/api/users/check-username?username=availableuser"),
            String::class.java
        )
        
        assertEquals(HttpStatus.OK, usernameResult.statusCode)
        assertNotNull(usernameResult.body)
        
        val usernameResponse = objectMapper.readTree(usernameResult.body)
        assertEquals(true, usernameResponse.get("available").asBoolean())

        // Check available email
        val emailResult = restTemplate.getForEntity(
            createUrl("/api/users/check-email?email=available@example.com"),
            String::class.java
        )
        
        assertEquals(HttpStatus.OK, emailResult.statusCode)
        assertNotNull(emailResult.body)
        
        val emailResponse = objectMapper.readTree(emailResult.body)
        assertEquals(true, emailResponse.get("available").asBoolean())
    }
}

