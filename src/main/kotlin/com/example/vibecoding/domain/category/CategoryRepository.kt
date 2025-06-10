package com.example.vibecoding.domain.category

/**
 * Repository interface for Category domain (Port in hexagonal architecture)
 */
interface CategoryRepository {
    fun save(category: Category): Category
    fun findById(id: CategoryId): Category?
    fun findAll(): List<Category>
    fun findByName(name: String): Category?
    fun delete(id: CategoryId): Boolean
    fun existsById(id: CategoryId): Boolean
    fun existsByName(name: String): Boolean
}

