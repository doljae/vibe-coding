package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of UserRepository for testing purposes
 */
@Repository
class InMemoryUserRepository : UserRepository {
    private val users = ConcurrentHashMap<UserId, User>()

    override fun save(user: User): User {
        users[user.id] = user
        return user
    }

    override fun findById(id: UserId): User? {
        return users[id]
    }

    override fun findByUsername(username: String): User? {
        return users.values.find { it.username == username }
    }

    override fun findByEmail(email: String): User? {
        return users.values.find { it.email == email }
    }

    override fun findAll(): List<User> {
        return users.values.toList()
    }

    override fun deleteById(id: UserId): Boolean {
        return users.remove(id) != null
    }

    override fun existsByUsername(username: String): Boolean {
        return users.values.any { it.username == username }
    }

    override fun existsByEmail(email: String): Boolean {
        return users.values.any { it.email == email }
    }

    // Additional method for testing
    fun clear() {
        users.clear()
    }

    fun size(): Int {
        return users.size
    }
}

