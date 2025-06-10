package com.example.vibecoding.application.category

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.PostRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CategoryServiceTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var postRepository: PostRepository
    private lateinit var categoryService: CategoryService

    @BeforeEach
    fun setUp() {
        categoryRepository = mockk(relaxed = true)
        postRepository = mockk(relaxed = true)
        categoryService = CategoryService(categoryRepository, postRepository)
    }

    @Test
    fun `should create category successfully`() {
        // Given
        val name = "Technology"
        val description = "Tech related posts"
        
        every { categoryRepository.existsByName(name) } returns false
        every { categoryRepository.save(any()) } returnsArgument 0

        // When
        val result = categoryService.createCategory(name, description)

        // Then
        result.name shouldBe name
        result.description shouldBe description
        
        verify { categoryRepository.existsByName(name) }
        verify { categoryRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating category with existing name`() {
        // Given
        val name = "Technology"
        val description = "Tech related posts"
        
        every { categoryRepository.existsByName(name) } returns true

        // When & Then
        shouldThrow<CategoryAlreadyExistsException> {
            categoryService.createCategory(name, description)
        }
        
        verify { categoryRepository.existsByName(name) }
        verify(exactly = 0) { categoryRepository.save(any()) }
    }

    @Test
    fun `should update category successfully`() {
        // Given
        val categoryId = CategoryId.generate()
        val existingCategory = createTestCategory(categoryId, "Technology", "Old description")
        val newName = "Updated Technology"
        val newDescription = "Updated description"
        
        every { categoryRepository.findById(categoryId) } returns existingCategory
        every { categoryRepository.existsByName(newName) } returns false
        every { categoryRepository.save(any()) } returnsArgument 0

        // When
        val result = categoryService.updateCategory(categoryId, newName, newDescription)

        // Then
        result.name shouldBe newName
        result.description shouldBe newDescription
        
        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.existsByName(newName) }
        verify { categoryRepository.save(any()) }
    }

    @Test
    fun `should throw exception when updating non-existent category`() {
        // Given
        val categoryId = CategoryId.generate()
        
        every { categoryRepository.findById(categoryId) } returns null

        // When & Then
        shouldThrow<CategoryNotFoundException> {
            categoryService.updateCategory(categoryId, "New Name", "New Description")
        }
        
        verify { categoryRepository.findById(categoryId) }
        verify(exactly = 0) { categoryRepository.save(any()) }
    }

    @Test
    fun `should throw exception when updating with existing name`() {
        // Given
        val categoryId = CategoryId.generate()
        val existingCategory = createTestCategory(categoryId, "Technology", "Description")
        val newName = "Existing Name"
        
        every { categoryRepository.findById(categoryId) } returns existingCategory
        every { categoryRepository.existsByName(newName) } returns true

        // When & Then
        shouldThrow<CategoryAlreadyExistsException> {
            categoryService.updateCategory(categoryId, newName, null)
        }
        
        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.existsByName(newName) }
        verify(exactly = 0) { categoryRepository.save(any()) }
    }

    @Test
    fun `should get category by id successfully`() {
        // Given
        val categoryId = CategoryId.generate()
        val category = createTestCategory(categoryId, "Technology", "Description")
        
        every { categoryRepository.findById(categoryId) } returns category

        // When
        val result = categoryService.getCategoryById(categoryId)

        // Then
        result shouldBe category
        
        verify { categoryRepository.findById(categoryId) }
    }

    @Test
    fun `should throw exception when getting non-existent category`() {
        // Given
        val categoryId = CategoryId.generate()
        
        every { categoryRepository.findById(categoryId) } returns null

        // When & Then
        shouldThrow<CategoryNotFoundException> {
            categoryService.getCategoryById(categoryId)
        }
        
        verify { categoryRepository.findById(categoryId) }
    }

    @Test
    fun `should get all categories successfully`() {
        // Given
        val categories = listOf(
            createTestCategory(CategoryId.generate(), "Technology", "Tech"),
            createTestCategory(CategoryId.generate(), "Science", "Science")
        )
        
        every { categoryRepository.findAll() } returns categories

        // When
        val result = categoryService.getAllCategories()

        // Then
        result shouldBe categories
        
        verify { categoryRepository.findAll() }
    }

    @Test
    fun `should get category by name successfully`() {
        // Given
        val name = "Technology"
        val category = createTestCategory(CategoryId.generate(), name, "Description")
        
        every { categoryRepository.findByName(name) } returns category

        // When
        val result = categoryService.getCategoryByName(name)

        // Then
        result shouldBe category
        
        verify { categoryRepository.findByName(name) }
    }

    @Test
    fun `should return null when category not found by name`() {
        // Given
        val name = "NonExistent"
        
        every { categoryRepository.findByName(name) } returns null

        // When
        val result = categoryService.getCategoryByName(name)

        // Then
        result shouldBe null
        
        verify { categoryRepository.findByName(name) }
    }

    @Test
    fun `should delete category successfully`() {
        // Given
        val categoryId = CategoryId.generate()
        
        every { categoryRepository.existsById(categoryId) } returns true
        every { postRepository.countByCategoryId(categoryId) } returns 0
        every { categoryRepository.delete(categoryId) } returns true

        // When
        categoryService.deleteCategory(categoryId)

        // Then
        verify { categoryRepository.existsById(categoryId) }
        verify { postRepository.countByCategoryId(categoryId) }
        verify { categoryRepository.delete(categoryId) }
    }

    @Test
    fun `should throw exception when deleting non-existent category`() {
        // Given
        val categoryId = CategoryId.generate()
        
        every { categoryRepository.existsById(categoryId) } returns false

        // When & Then
        shouldThrow<CategoryNotFoundException> {
            categoryService.deleteCategory(categoryId)
        }
        
        verify { categoryRepository.existsById(categoryId) }
    }

    @Test
    fun `should throw exception when deleting category with posts`() {
        // Given
        val categoryId = CategoryId.generate()
        
        every { categoryRepository.existsById(categoryId) } returns true
        every { postRepository.countByCategoryId(categoryId) } returns 5

        // When & Then
        shouldThrow<CategoryHasPostsException> {
            categoryService.deleteCategory(categoryId)
        }
        
        verify { categoryRepository.existsById(categoryId) }
        verify { postRepository.countByCategoryId(categoryId) }
    }

    private fun createTestCategory(id: CategoryId, name: String, description: String): Category {
        val now = LocalDateTime.now()
        return Category(
            id = id,
            name = name,
            description = description,
            createdAt = now,
            updatedAt = now
        )
    }
}

