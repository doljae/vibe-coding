package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of CategoryRepository (Adapter in hexagonal architecture)
 */
@Repository
class InMemoryCategoryRepository : CategoryRepository {
    
    private val categories = ConcurrentHashMap<CategoryId, Category>()

    override fun save(category: Category): Category {
        categories[category.id] = category
        return category
    }

    override fun findById(id: CategoryId): Category? {
        return categories[id]
    }

    override fun findAll(): List<Category> {
        return categories.values.sortedBy { it.createdAt }
    }

    override fun findByName(name: String): Category? {
        return categories.values.find { it.name.equals(name, ignoreCase = true) }
    }

    override fun delete(id: CategoryId): Boolean {
        return categories.remove(id) != null
    }

    override fun existsById(id: CategoryId): Boolean {
        return categories.containsKey(id)
    }

    override fun existsByName(name: String): Boolean {
        return categories.values.any { it.name.equals(name, ignoreCase = true) }
    }
}

