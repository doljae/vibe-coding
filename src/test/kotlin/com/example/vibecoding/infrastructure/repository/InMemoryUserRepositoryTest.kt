package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryUserRepositoryTest {

    private lateinit var repository: InMemoryUserRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryUserRepository()
    }

    @Test
    fun `should save and find user by id`() {
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = "Test bio",
            createdAt = now,
            updatedAt = now
        )

        val savedUser = repository.save(user)
        val foundUser = repository.findById(user.id)

        assertEquals(savedUser, foundUser)
        assertEquals(user.username, foundUser?.username)
    }

    @Test
    fun `should return null when user not found by id`() {
        val nonExistentId = UserId.generate()
        val foundUser = repository.findById(nonExistentId)

        assertNull(foundUser)
    }

    @Test
    fun `should find user by username`() {
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        repository.save(user)
        val foundUser = repository.findByUsername("testuser")

        assertEquals(user, foundUser)
    }

    @Test
    fun `should return null when user not found by username`() {
        val foundUser = repository.findByUsername("nonexistent")

        assertNull(foundUser)
    }

    @Test
    fun `should find user by email`() {
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        repository.save(user)
        val foundUser = repository.findByEmail("test@example.com")

        assertEquals(user, foundUser)
    }

    @Test
    fun `should return null when user not found by email`() {
        val foundUser = repository.findByEmail("nonexistent@example.com")

        assertNull(foundUser)
    }

    @Test
    fun `should find all users`() {
        val now = LocalDateTime.now()
        val user1 = User(
            id = UserId.generate(),
            username = "user1",
            email = "user1@example.com",
            displayName = "User 1",
            bio = null,
            createdAt = now,
            updatedAt = now
        )
        val user2 = User(
            id = UserId.generate(),
            username = "user2",
            email = "user2@example.com",
            displayName = "User 2",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        repository.save(user1)
        repository.save(user2)

        val allUsers = repository.findAll()

        assertEquals(2, allUsers.size)
        assertTrue(allUsers.contains(user1))
        assertTrue(allUsers.contains(user2))
    }

    @Test
    fun `should delete user by id`() {
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        repository.save(user)
        val deleted = repository.deleteById(user.id)
        val foundUser = repository.findById(user.id)

        assertTrue(deleted)
        assertNull(foundUser)
    }

    @Test
    fun `should return false when deleting non-existent user`() {
        val nonExistentId = UserId.generate()
        val deleted = repository.deleteById(nonExistentId)

        assertFalse(deleted)
    }

    @Test
    fun `should check if username exists`() {
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        repository.save(user)

        assertTrue(repository.existsByUsername("testuser"))
        assertFalse(repository.existsByUsername("nonexistent"))
    }

    @Test
    fun `should check if email exists`() {
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        repository.save(user)

        assertTrue(repository.existsByEmail("test@example.com"))
        assertFalse(repository.existsByEmail("nonexistent@example.com"))
    }

    @Test
    fun `should update existing user`() {
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        repository.save(user)
        val updatedUser = user.copy(displayName = "Updated Name")
        repository.save(updatedUser)

        val foundUser = repository.findById(user.id)
        assertEquals("Updated Name", foundUser?.displayName)
    }

    @Test
    fun `should clear all users`() {
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        repository.save(user)
        assertEquals(1, repository.findAll().size)

        repository.clear()
        assertEquals(0, repository.findAll().size)
        assertTrue(repository.findAll().isEmpty())
    }
}

