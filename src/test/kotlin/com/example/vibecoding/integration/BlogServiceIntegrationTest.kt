package com.example.vibecoding.integration

import com.example.vibecoding.application.category.CategoryService
import com.example.vibecoding.application.post.PostService
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.PostId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test demonstrating the complete blog service functionality
 * with hexagonal architecture using in-memory repositories
 */
@SpringBootTest
class BlogServiceIntegrationTest {

    @Autowired
    private lateinit var categoryService: CategoryService

    @Autowired
    private lateinit var postService: PostService

    @Test
    fun `should demonstrate complete blog service workflow`() {
        // Create categories
        val techCategory = categoryService.createCategory("Technology", "All about tech")
        val scienceCategory = categoryService.createCategory("Science", "Scientific discoveries")

        // Verify categories were created
        assertNotNull(techCategory.id)
        assertEquals("Technology", techCategory.name)
        assertEquals("All about tech", techCategory.description)

        // Get all categories
        val allCategories = categoryService.getAllCategories()
        assertEquals(2, allCategories.size)

        // Create posts
        val post1 = postService.createPost(
            "Introduction to Kotlin",
            "Kotlin is a modern programming language that runs on the JVM...",
            techCategory.id
        )

        val post2 = postService.createPost(
            "Spring Boot Best Practices",
            "Here are some best practices for Spring Boot development...",
            techCategory.id
        )

        val post3 = postService.createPost(
            "Quantum Computing Basics",
            "Quantum computing represents a new paradigm...",
            scienceCategory.id
        )

        // Verify posts were created
        assertNotNull(post1.id)
        assertEquals("Introduction to Kotlin", post1.title)
        assertEquals(techCategory.id, post1.categoryId)
        
        // Verify post2 was created
        assertNotNull(post2.id)
        assertEquals("Spring Boot Best Practices", post2.title)
        assertEquals(techCategory.id, post2.categoryId)

        // Get all posts
        val allPosts = postService.getAllPosts()
        assertEquals(3, allPosts.size)

        // Get posts by category
        val techPosts = postService.getPostsByCategory(techCategory.id)
        assertEquals(2, techPosts.size)
        assertTrue(techPosts.any { it.title == "Introduction to Kotlin" })
        assertTrue(techPosts.any { it.title == "Spring Boot Best Practices" })

        val sciencePosts = postService.getPostsByCategory(scienceCategory.id)
        assertEquals(1, sciencePosts.size)
        assertEquals("Quantum Computing Basics", sciencePosts[0].title)

        // Search posts by title
        val kotlinPosts = postService.searchPostsByTitle("Kotlin")
        assertEquals(1, kotlinPosts.size)
        assertEquals("Introduction to Kotlin", kotlinPosts[0].title)

        // Update a post
        val updatedPost = postService.updatePost(
            post1.id,
            "Advanced Kotlin Programming",
            "This is an updated content about advanced Kotlin features...",
            null
        )
        assertEquals("Advanced Kotlin Programming", updatedPost.title)
        assertEquals("This is an updated content about advanced Kotlin features...", updatedPost.content)

        // Update a category
        val updatedCategory = categoryService.updateCategory(
            techCategory.id,
            "Advanced Technology",
            "Advanced tech topics and tutorials"
        )
        assertEquals("Advanced Technology", updatedCategory.name)
        assertEquals("Advanced tech topics and tutorials", updatedCategory.description)

        // Verify we can still find the category by its new name
        val foundCategory = categoryService.getCategoryByName("Advanced Technology")
        assertNotNull(foundCategory)
        assertEquals(techCategory.id, foundCategory.id)

        // Delete a post
        postService.deletePost(post3.id)
        val remainingPosts = postService.getAllPosts()
        assertEquals(2, remainingPosts.size)

        // Verify we can't delete a category that has posts
        val techPostsCount = postService.getPostsByCategory(techCategory.id).size
        assertTrue(techPostsCount > 0)

        // Delete all posts from tech category first
        val techPostsToDelete = postService.getPostsByCategory(techCategory.id)
        techPostsToDelete.forEach { post ->
            postService.deletePost(post.id)
        }

        // Now we can delete the category
        categoryService.deleteCategory(techCategory.id)
        val finalCategories = categoryService.getAllCategories()
        assertEquals(1, finalCategories.size)
        assertEquals("Science", finalCategories[0].name)
    }

    @Test
    fun `should handle edge cases and validations`() {
        // Create a category
        val category = categoryService.createCategory("Test Category", "Test description")
        
        // Verify category was created
        assertNotNull(category.id)
        assertEquals("Test Category", category.name)

        // Try to create a category with the same name (should fail)
        try {
            categoryService.createCategory("Test Category", "Another description")
            throw AssertionError("Should have thrown CategoryAlreadyExistsException")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("already exists") == true)
        }

        // Try to create a post with non-existent category (should fail)
        val nonExistentCategoryId = CategoryId.generate()
        try {
            postService.createPost("Test Post", "Test content", nonExistentCategoryId)
            throw AssertionError("Should have thrown CategoryNotFoundException")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("not found") == true)
        }

        // Try to get non-existent post (should fail)
        val nonExistentPostId = PostId.generate()
        try {
            postService.getPostById(nonExistentPostId)
            throw AssertionError("Should have thrown PostNotFoundException")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("not found") == true)
        }

        // Try to delete non-existent category (should fail)
        val anotherNonExistentCategoryId = CategoryId.generate()
        try {
            categoryService.deleteCategory(anotherNonExistentCategoryId)
            throw AssertionError("Should have thrown CategoryNotFoundException")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("not found") == true)
        }
    }
}
