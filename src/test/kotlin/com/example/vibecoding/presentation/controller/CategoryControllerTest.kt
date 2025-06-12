package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.category.CategoryAlreadyExistsException
import com.example.vibecoding.application.category.CategoryHasPostsException
import com.example.vibecoding.application.category.CategoryNotFoundException
import com.example.vibecoding.application.category.CategoryService
import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.presentation.dto.CreateCategoryRequest
import com.example.vibecoding.presentation.dto.UpdateCategoryRequest
import com.example.vibecoding.presentation.exception.GlobalExceptionHandler
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

import java.time.LocalDateTime
import java.util.*

class CategoryControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var categoryService: CategoryService
    private lateinit var objectMapper: ObjectMapper

    private val testCategoryId = CategoryId.generate()
    private val testCategory = Category(
        id = testCategoryId,
        name = "Technology",
        description = "Technology related posts",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        categoryService = mockk()
        objectMapper = ObjectMapper()
        objectMapper.findAndRegisterModules()
        
        val controller = CategoryController(categoryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @Test
    fun `getAllCategories should return list of categories`() {
        // Given
        val categories = listOf(testCategory)
        every { categoryService.getAllCategories() } returns categories

        // When & Then
        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").value(testCategoryId.value.toString()))
            .andExpect(jsonPath("$[0].name").value("Technology"))
            .andExpect(jsonPath("$[0].description").value("Technology related posts"))

        verify { categoryService.getAllCategories() }
    }

    @Test
    fun `getAllCategories should return empty list when no categories exist`() {
        // Given
        every { categoryService.getAllCategories() } returns emptyList()

        // When & Then
        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

        verify { categoryService.getAllCategories() }
    }

    @Test
    fun `getCategoryById should return category when found`() {
        // Given
        every { categoryService.getCategoryById(testCategoryId) } returns testCategory

        // When & Then
        mockMvc.perform(get("/api/categories/${testCategoryId.value}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testCategoryId.value.toString()))
            .andExpect(jsonPath("$.name").value("Technology"))
            .andExpect(jsonPath("$.description").value("Technology related posts"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists())

        verify { categoryService.getCategoryById(testCategoryId) }
    }

    @Test
    fun `getCategoryById should return 404 when category not found`() {
        // Given
        every { categoryService.getCategoryById(testCategoryId) } throws CategoryNotFoundException("Category not found")

        // When & Then
        mockMvc.perform(get("/api/categories/${testCategoryId.value}"))
            .andExpect(status().isNotFound)

        verify { categoryService.getCategoryById(testCategoryId) }
    }

    @Test
    fun `getCategoryById should return 400 for invalid UUID`() {
        // When & Then
        mockMvc.perform(get("/api/categories/invalid-uuid"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `createCategory should create and return category`() {
        // Given
        val request = CreateCategoryRequest(
            name = "Technology",
            description = "Technology related posts"
        )
        every { categoryService.createCategory("Technology", "Technology related posts") } returns testCategory

        // When & Then
        mockMvc.perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testCategoryId.value.toString()))
            .andExpect(jsonPath("$.name").value("Technology"))
            .andExpect(jsonPath("$.description").value("Technology related posts"))

        verify { categoryService.createCategory("Technology", "Technology related posts") }
    }

    @Test
    fun `createCategory should return 400 for invalid request`() {
        // Given
        val request = CreateCategoryRequest(
            name = "", // Invalid: blank name
            description = "Technology related posts"
        )
        
        // Mock service to throw IllegalArgumentException for invalid input
        every { categoryService.createCategory("", "Technology related posts") } throws IllegalArgumentException("Category name cannot be blank")

        // When & Then
        mockMvc.perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `createCategory should return 409 when category already exists`() {
        // Given
        val request = CreateCategoryRequest(
            name = "Technology",
            description = "Technology related posts"
        )
        every { categoryService.createCategory("Technology", "Technology related posts") } throws 
            CategoryAlreadyExistsException("Category already exists")

        // When & Then
        mockMvc.perform(
            post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict)

        verify { categoryService.createCategory("Technology", "Technology related posts") }
    }

    @Test
    fun `updateCategory should update and return category`() {
        // Given
        val request = UpdateCategoryRequest(
            name = "Updated Technology",
            description = "Updated description"
        )
        val updatedCategory = testCategory.copy(
            name = "Updated Technology",
            description = "Updated description"
        )
        every { categoryService.updateCategory(testCategoryId, "Updated Technology", "Updated description") } returns updatedCategory

        // When & Then
        mockMvc.perform(
            put("/api/categories/${testCategoryId.value}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testCategoryId.value.toString()))
            .andExpect(jsonPath("$.name").value("Updated Technology"))
            .andExpect(jsonPath("$.description").value("Updated description"))

        verify { categoryService.updateCategory(testCategoryId, "Updated Technology", "Updated description") }
    }

    @Test
    fun `updateCategory should return 404 when category not found`() {
        // Given
        val request = UpdateCategoryRequest(name = "Updated Technology")
        every { categoryService.updateCategory(testCategoryId, "Updated Technology", null) } throws 
            CategoryNotFoundException("Category not found")

        // When & Then
        mockMvc.perform(
            put("/api/categories/${testCategoryId.value}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)

        verify { categoryService.updateCategory(testCategoryId, "Updated Technology", null) }
    }

    @Test
    fun `deleteCategory should delete category successfully`() {
        // Given
        every { categoryService.deleteCategory(testCategoryId) } returns Unit

        // When & Then
        mockMvc.perform(delete("/api/categories/${testCategoryId.value}"))
            .andExpect(status().isNoContent)

        verify { categoryService.deleteCategory(testCategoryId) }
    }

    @Test
    fun `deleteCategory should return 404 when category not found`() {
        // Given
        every { categoryService.deleteCategory(testCategoryId) } throws CategoryNotFoundException("Category not found")

        // When & Then
        mockMvc.perform(delete("/api/categories/${testCategoryId.value}"))
            .andExpect(status().isNotFound)

        verify { categoryService.deleteCategory(testCategoryId) }
    }

    @Test
    fun `deleteCategory should return 409 when category has posts`() {
        // Given
        every { categoryService.deleteCategory(testCategoryId) } throws CategoryHasPostsException("Category has posts")

        // When & Then
        mockMvc.perform(delete("/api/categories/${testCategoryId.value}"))
            .andExpect(status().isConflict)

        verify { categoryService.deleteCategory(testCategoryId) }
    }

    @Test
    fun `getCategoryByName should return category when found`() {
        // Given
        every { categoryService.getCategoryByName("Technology") } returns testCategory

        // When & Then
        mockMvc.perform(get("/api/categories/search").param("name", "Technology"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testCategoryId.value.toString()))
            .andExpect(jsonPath("$.name").value("Technology"))

        verify { categoryService.getCategoryByName("Technology") }
    }

    @Test
    fun `getCategoryByName should return 404 when category not found`() {
        // Given
        every { categoryService.getCategoryByName("NonExistent") } returns null

        // When & Then
        mockMvc.perform(get("/api/categories/search").param("name", "NonExistent"))
            .andExpect(status().isNotFound)

        verify { categoryService.getCategoryByName("NonExistent") }
    }
}
