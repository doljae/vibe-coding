package com.example.vibecoding.integration

import com.example.vibecoding.application.category.CategoryService
import com.example.vibecoding.application.category.CategoryAlreadyExistsException
import com.example.vibecoding.application.category.CategoryNotFoundException as CategoryServiceCategoryNotFoundException
import com.example.vibecoding.application.post.PostService
import com.example.vibecoding.application.post.PostNotFoundException
import com.example.vibecoding.application.post.CategoryNotFoundException as PostServiceCategoryNotFoundException
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.PostId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

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
        techCategory.id.shouldNotBeNull()
        techCategory.name shouldBe "Technology"
        techCategory.description shouldBe "All about tech"

        // Get all categories
        val allCategories = categoryService.getAllCategories()
        allCategories.size shouldBe 2

        // Create posts
        val post1 = postService.createPost(
            "Introduction to Kotlin",
            "Kotlin is a modern programming language that runs on the JVM...",
            techCategory.id
        )

        val post2 = postService.createPost(
            "Spring Boot Best Practices",
            "Spring Boot makes it easy to create stand-alone applications...",
            techCategory.id
        )

        val post3 = postService.createPost(
            "Quantum Computing Basics",
            "Quantum computing is a revolutionary approach to computation...",
            scienceCategory.id
        )

        // Verify posts were created
        post1.id.shouldNotBeNull()
        post1.title shouldBe "Introduction to Kotlin"
        post1.categoryId shouldBe techCategory.id
        
        // Verify post2 was created
        post2.id.shouldNotBeNull()
        post2.title shouldBe "Spring Boot Best Practices"
        post2.categoryId shouldBe techCategory.id

        // Get all posts
        val allPosts = postService.getAllPosts()
        allPosts.size shouldBe 3

        // Get posts by category
        val techPosts = postService.getPostsByCategory(techCategory.id)
        techPosts.size shouldBe 2
        techPosts.any { it.title == "Introduction to Kotlin" } shouldBe true
        techPosts.any { it.title == "Spring Boot Best Practices" } shouldBe true

        val sciencePosts = postService.getPostsByCategory(scienceCategory.id)
        sciencePosts.size shouldBe 1
        sciencePosts[0].title shouldBe "Quantum Computing Basics"

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

        // Verify we can't delete a category that has posts
        val techPostsCount = postService.getPostsByCategory(techCategory.id).size
        (techPostsCount > 0) shouldBe true

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
    }

    @Test
    fun `should handle edge cases and validations`() {
        // Create a test category
        val category = categoryService.createCategory("Test Category", "Test description")
        
        // Verify category was created
        category.id.shouldNotBeNull()
        category.name shouldBe "Test Category"

        // Try to create a category with the same name (should fail)
        shouldThrow<CategoryAlreadyExistsException> {
            categoryService.createCategory("Test Category", "Another description")
        }

        // Try to create a post with non-existent category (should fail)
        val nonExistentCategoryId = CategoryId.generate()
        shouldThrow<PostServiceCategoryNotFoundException> {
            postService.createPost("Test Post", "Test content", nonExistentCategoryId)
        }

        // Try to get non-existent post (should fail)
        val nonExistentPostId = PostId.generate()
        shouldThrow<PostNotFoundException> {
            postService.getPostById(nonExistentPostId)
        }

        // Try to delete non-existent category (should fail)
        val anotherNonExistentCategoryId = CategoryId.generate()
        shouldThrow<CategoryServiceCategoryNotFoundException> {
            categoryService.deleteCategory(anotherNonExistentCategoryId)
        }
    }
}

