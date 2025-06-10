package com.example.vibecoding.integration

import com.example.vibecoding.application.category.CategoryService
import com.example.vibecoding.application.category.CategoryAlreadyExistsException
import com.example.vibecoding.application.category.CategoryNotFoundException as CategoryServiceCategoryNotFoundException
import com.example.vibecoding.application.post.PostService
import com.example.vibecoding.application.post.PostNotFoundException
import com.example.vibecoding.application.post.CategoryNotFoundException as PostServiceCategoryNotFoundException
import com.example.vibecoding.application.post.UserNotFoundException as PostServiceUserNotFoundException
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

/**
 * Integration test demonstrating the complete blog service functionality
 * with hexagonal architecture using in-memory repositories
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BlogServiceIntegrationTest {

    @Autowired
    private lateinit var categoryService: CategoryService

    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var userService: UserService

    @Test
    fun `should demonstrate complete blog service workflow with users`() {
        // Create users
        val author1 = userService.createUser("john_doe", "john@example.com", "John Doe", "Software developer")
        val author2 = userService.createUser("jane_smith", "jane@example.com", "Jane Smith", "Tech writer")

        // Verify users were created
        author1.id.shouldNotBeNull()
        author1.username shouldBe "john_doe"
        author1.email shouldBe "john@example.com"
        author1.displayName shouldBe "John Doe"

        // Create categories
        val techCategory = categoryService.createCategory("Technology", "All about tech")
        val scienceCategory = categoryService.createCategory("Science", "Scientific discoveries")

        // Verify categories were created
        techCategory.id.shouldNotBeNull()
        techCategory.name shouldBe "Technology"
        techCategory.description shouldBe "All about tech"

        // Get all categories
        val allCategories = categoryService.getAllCategories()
        allCategories.size shouldBe 2

        // Create posts with authors
        val post1 = postService.createPost(
            "Introduction to Kotlin",
            "Kotlin is a modern programming language that runs on the JVM...",
            author1.id,
            techCategory.id
        )

        val post2 = postService.createPost(
            "Spring Boot Best Practices",
            "Spring Boot makes it easy to create stand-alone applications...",
            author1.id,
            techCategory.id
        )

        val post3 = postService.createPost(
            "Quantum Computing Basics",
            "Quantum computing is a revolutionary approach to computation...",
            author2.id,
            scienceCategory.id
        )

        // Verify posts were created with correct authors
        post1.id.shouldNotBeNull()
        post1.title shouldBe "Introduction to Kotlin"
        post1.authorId shouldBe author1.id
        post1.categoryId shouldBe techCategory.id
        
        post2.id.shouldNotBeNull()
        post2.title shouldBe "Spring Boot Best Practices"
        post2.authorId shouldBe author1.id
        post2.categoryId shouldBe techCategory.id

        post3.id.shouldNotBeNull()
        post3.title shouldBe "Quantum Computing Basics"
        post3.authorId shouldBe author2.id
        post3.categoryId shouldBe scienceCategory.id

        // Get all posts
        val allPosts = postService.getAllPosts()
        allPosts.size shouldBe 3

        // Get posts by category
        val techPosts = postService.getPostsByCategory(techCategory.id)
        techPosts.size shouldBe 2
        techPosts.any { it.title == "Introduction to Kotlin" } shouldBe true
        techPosts.any { it.title == "Spring Boot Best Practices" } shouldBe true

        // Get posts by author
        val author1Posts = postService.getPostsByAuthor(author1.id)
        author1Posts.size shouldBe 2
        author1Posts.all { it.authorId == author1.id } shouldBe true

        val author2Posts = postService.getPostsByAuthor(author2.id)
        author2Posts.size shouldBe 1
        author2Posts[0].title shouldBe "Quantum Computing Basics"

        // Get post counts
        val author1PostCount = postService.getPostCountByAuthor(author1.id)
        author1PostCount shouldBe 2L

        val techPostCount = postService.getPostCountByCategory(techCategory.id)
        techPostCount shouldBe 2L

        // Search posts by title
        val kotlinPosts = postService.searchPostsByTitle("Kotlin")
        kotlinPosts.size shouldBe 1
        kotlinPosts[0].title shouldBe "Introduction to Kotlin"

        // Update a post
        val updatedPost = postService.updatePost(
            post1.id,
            "Advanced Kotlin Programming",
            "This is an updated content about advanced Kotlin features...",
            null
        )
        updatedPost.title shouldBe "Advanced Kotlin Programming"
        updatedPost.content shouldBe "This is an updated content about advanced Kotlin features..."
        updatedPost.authorId shouldBe author1.id // Author should remain the same

        // Update user information
        val updatedUser = userService.updateUserDisplayName(author1.id, "John Doe Senior")
        updatedUser.displayName shouldBe "John Doe Senior"

        val updatedUserBio = userService.updateUserBio(author1.id, "Senior Software Developer and Tech Lead")
        updatedUserBio.bio shouldBe "Senior Software Developer and Tech Lead"

        // Update a category
        val updatedCategory = categoryService.updateCategory(
            techCategory.id,
            "Advanced Technology",
            "Advanced tech topics and tutorials"
        )
        updatedCategory.name shouldBe "Advanced Technology"
        updatedCategory.description shouldBe "Advanced tech topics and tutorials"

        // Verify we can still find the category by its new name
        val foundCategory = categoryService.getCategoryByName("Advanced Technology")
        foundCategory.shouldNotBeNull()
        foundCategory.id shouldBe techCategory.id

        // Delete a post
        postService.deletePost(post3.id)
        val remainingPosts = postService.getAllPosts()
        remainingPosts.size shouldBe 2

        // Verify post count updated
        val updatedAuthor2PostCount = postService.getPostCountByAuthor(author2.id)
        updatedAuthor2PostCount shouldBe 0L

        // Delete all posts from tech category first
        val techPostsToDelete = postService.getPostsByCategory(techCategory.id)
        techPostsToDelete.forEach { post ->
            postService.deletePost(post.id)
        }

        // Now we can delete the category
        categoryService.deleteCategory(techCategory.id)
        val finalCategories = categoryService.getAllCategories()
        finalCategories.size shouldBe 1
        finalCategories[0].name shouldBe "Science"

        // Test user management
        val allUsers = userService.getAllUsers()
        allUsers.size shouldBe 2

        // Test username and email availability
        val isUsernameAvailable = userService.isUsernameAvailable("new_user")
        isUsernameAvailable shouldBe true

        val isEmailAvailable = userService.isEmailAvailable("john@example.com")
        isEmailAvailable shouldBe false // Already taken by author1

        // Delete a user
        userService.deleteUser(author2.id)
        val remainingUsers = userService.getAllUsers()
        remainingUsers.size shouldBe 1
        remainingUsers[0].username shouldBe "john_doe"
    }

    @Test
    fun `should handle edge cases and validations with users`() {
        // Create test data
        val user = userService.createUser("testuser", "test@example.com", "Test User")
        val category = categoryService.createCategory("Test Category", "Test description")
        
        // Verify user was created
        user.id.shouldNotBeNull()
        user.username shouldBe "testuser"

        // Try to create a user with the same username (should fail)
        shouldThrow<IllegalArgumentException> {
            userService.createUser("testuser", "another@example.com", "Another User")
        }

        // Try to create a user with the same email (should fail)
        shouldThrow<IllegalArgumentException> {
            userService.createUser("anotheruser", "test@example.com", "Another User")
        }

        // Try to create a category with the same name (should fail)
        shouldThrow<CategoryAlreadyExistsException> {
            categoryService.createCategory("Test Category", "Another description")
        }

        // Try to create a post with non-existent user (should fail)
        val nonExistentUserId = UserId.generate()
        shouldThrow<PostServiceUserNotFoundException> {
            postService.createPost("Test Post", "Test content", nonExistentUserId, category.id)
        }

        // Try to create a post with non-existent category (should fail)
        val nonExistentCategoryId = CategoryId.generate()
        shouldThrow<PostServiceCategoryNotFoundException> {
            postService.createPost("Test Post", "Test content", user.id, nonExistentCategoryId)
        }

        // Try to get posts by non-existent author (should fail)
        shouldThrow<PostServiceUserNotFoundException> {
            postService.getPostsByAuthor(nonExistentUserId)
        }

        // Try to get non-existent post (should fail)
        val nonExistentPostId = PostId.generate()
        shouldThrow<PostNotFoundException> {
            postService.getPostById(nonExistentPostId)
        }

        // Try to update non-existent user (should fail)
        shouldThrow<IllegalArgumentException> {
            userService.updateUserDisplayName(nonExistentUserId, "New Name")
        }

        // Try to delete non-existent user (should fail)
        shouldThrow<IllegalArgumentException> {
            userService.deleteUser(nonExistentUserId)
        }

        // Try to delete non-existent category (should fail)
        val anotherNonExistentCategoryId = CategoryId.generate()
        shouldThrow<CategoryServiceCategoryNotFoundException> {
            categoryService.deleteCategory(anotherNonExistentCategoryId)
        }
    }

    @Test
    fun `should validate user input constraints`() {
        // Test username validation
        shouldThrow<IllegalArgumentException> {
            userService.createUser("ab", "test@example.com", "Test User") // Too short
        }

        shouldThrow<IllegalArgumentException> {
            userService.createUser("user-name", "test@example.com", "Test User") // Invalid characters
        }

        // Test email validation
        shouldThrow<IllegalArgumentException> {
            userService.createUser("testuser", "invalid-email", "Test User") // Invalid email format
        }

        // Test display name validation
        shouldThrow<IllegalArgumentException> {
            userService.createUser("testuser", "test@example.com", "") // Empty display name
        }

        // Test bio length validation
        val longBio = "a".repeat(501)
        shouldThrow<IllegalArgumentException> {
            userService.createUser("testuser", "test@example.com", "Test User", longBio)
        }
    }
}
