package com.example.vibecoding.domain.user

/**
 * Repository interface for User domain (Port in hexagonal architecture)
 */
interface UserRepository {
    fun save(user: User): User
    fun findById(id: UserId): User?
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findAll(): List<User>
    fun deleteById(id: UserId): Boolean
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}

