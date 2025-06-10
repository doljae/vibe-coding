package com.example.vibecoding.application.category

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.PostRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Application service for Category domain operations
 */
@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val postRepository: PostRepository
) {

    fun createCategory(name: String, description: String?): Category {
        if (categoryRepository.existsByName(name)) {
            throw CategoryAlreadyExistsException("Category with name '$name' already exists")
        }

        val now = LocalDateTime.now()
        val category = Category(
            id = CategoryId.generate(),
            name = name,
            description = description,
            createdAt = now,
            updatedAt = now
        )

        return categoryRepository.save(category)
    }

    fun updateCategory(id: CategoryId, name: String?, description: String?): Category {
        val existingCategory = categoryRepository.findById(id)
            ?: throw CategoryNotFoundException("Category with id '$id' not found")

        var updatedCategory = existingCategory

        name?.let { newName ->
            if (newName != existingCategory.name && categoryRepository.existsByName(newName)) {
                throw CategoryAlreadyExistsException("Category with name '$newName' already exists")
            }
            updatedCategory = updatedCategory.updateName(newName)
        }

        description?.let { newDescription ->
            updatedCategory = updatedCategory.updateDescription(newDescription)
        }

        return categoryRepository.save(updatedCategory)
    }

    fun getCategoryById(id: CategoryId): Category {
        return categoryRepository.findById(id)
            ?: throw CategoryNotFoundException("Category with id '$id' not found")
    }

    fun getAllCategories(): List<Category> {
        return categoryRepository.findAll()
    }

    fun getCategoryByName(name: String): Category? {
        return categoryRepository.findByName(name)
    }

    fun deleteCategory(id: CategoryId) {
        if (!categoryRepository.existsById(id)) {
            throw CategoryNotFoundException("Category with id '$id' not found")
        }

        val postCount = postRepository.countByCategoryId(id)
        if (postCount > 0) {
            throw CategoryHasPostsException("Cannot delete category with id '$id' because it has $postCount posts")
        }

        categoryRepository.delete(id)
    }
}

class CategoryNotFoundException(message: String) : RuntimeException(message)
class CategoryAlreadyExistsException(message: String) : RuntimeException(message)
class CategoryHasPostsException(message: String) : RuntimeException(message)

