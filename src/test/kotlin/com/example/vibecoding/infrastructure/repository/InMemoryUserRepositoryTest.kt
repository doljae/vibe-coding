package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

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

        foundUser shouldBe savedUser
        foundUser?.username shouldBe user.username
    }

    @Test
    fun `should return null when user not found by id`() {
        val nonExistentId = UserId.generate()
        val foundUser = repository.findById(nonExistentId)

        foundUser.shouldBeNull()
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

        foundUser shouldBe user
    }

    @Test
    fun `should return null when user not found by username`() {
        val foundUser = repository.findByUsername("nonexistent")

        foundUser.shouldBeNull()
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

        foundUser shouldBe user
    }

    @Test
    fun `should return null when user not found by email`() {
        val foundUser = repository.findByEmail("nonexistent@example.com")

        foundUser.shouldBeNull()
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

        allUsers.size shouldBe 2
        allUsers shouldContain user1
        allUsers shouldContain user2
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

        deleted.shouldBeTrue()
        foundUser.shouldBeNull()
    }

    @Test
    fun `should return false when deleting non-existent user`() {
        val nonExistentId = UserId.generate()
        val deleted = repository.deleteById(nonExistentId)

        deleted.shouldBeFalse()
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

        repository.existsByUsername("testuser").shouldBeTrue()
        repository.existsByUsername("nonexistent").shouldBeFalse()
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

        repository.existsByEmail("test@example.com").shouldBeTrue()
        repository.existsByEmail("nonexistent@example.com").shouldBeFalse()
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
        val updatedUser = user.updateDisplayName("Updated Name")
        repository.save(updatedUser)

        val foundUser = repository.findById(user.id)
        foundUser?.displayName shouldBe "Updated Name"
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
        repository.size() shouldBe 1

        repository.clear()
        repository.size() shouldBe 0
        repository.findAll().shouldBeEmpty()
    }
}
