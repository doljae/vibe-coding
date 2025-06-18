package com.example.vibecoding.domain.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UserTest {

    @Test
    fun `should create user with valid data`() {
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = "This is a test bio",
            createdAt = now,
            updatedAt = now
        )

        user.username shouldBe "testuser"
        user.email shouldBe "test@example.com"
        user.displayName shouldBe "Test User"
        user.bio shouldBe "This is a test bio"
    }

    @Test
    fun `should create user with null bio`() {
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

        user.bio shouldBe null
    }

    @Test
    fun `should throw exception when username is blank`() {
        val now = LocalDateTime.now()
        
        shouldThrow<IllegalArgumentException> {
            User(
                id = UserId.generate(),
                username = "",
                email = "test@example.com",
                displayName = "Test User",
                bio = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when username is too short`() {
        val now = LocalDateTime.now()
        
        shouldThrow<IllegalArgumentException> {
            User(
                id = UserId.generate(),
                username = "ab",
                email = "test@example.com",
                displayName = "Test User",
                bio = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when username is too long`() {
        val now = LocalDateTime.now()
        val longUsername = "a".repeat(51)
        
        shouldThrow<IllegalArgumentException> {
            User(
                id = UserId.generate(),
                username = longUsername,
                email = "test@example.com",
                displayName = "Test User",
                bio = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when username contains invalid characters`() {
        val now = LocalDateTime.now()
        
        shouldThrow<IllegalArgumentException> {
            User(
                id = UserId.generate(),
                username = "test-user",
                email = "test@example.com",
                displayName = "Test User",
                bio = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when email is blank`() {
        val now = LocalDateTime.now()
        
        shouldThrow<IllegalArgumentException> {
            User(
                id = UserId.generate(),
                username = "testuser",
                email = "",
                displayName = "Test User",
                bio = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when email format is invalid`() {
        val now = LocalDateTime.now()
        
        shouldThrow<IllegalArgumentException> {
            User(
                id = UserId.generate(),
                username = "testuser",
                email = "invalid-email",
                displayName = "Test User",
                bio = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when display name is blank`() {
        val now = LocalDateTime.now()
        
        shouldThrow<IllegalArgumentException> {
            User(
                id = UserId.generate(),
                username = "testuser",
                email = "test@example.com",
                displayName = "",
                bio = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when display name is too long`() {
        val now = LocalDateTime.now()
        val longDisplayName = "a".repeat(101)
        
        shouldThrow<IllegalArgumentException> {
            User(
                id = UserId.generate(),
                username = "testuser",
                email = "test@example.com",
                displayName = longDisplayName,
                bio = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when bio is too long`() {
        val now = LocalDateTime.now()
        val longBio = "a".repeat(501)
        
        shouldThrow<IllegalArgumentException> {
            User(
                id = UserId.generate(),
                username = "testuser",
                email = "test@example.com",
                displayName = "Test User",
                bio = longBio,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should update display name successfully`() {
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

        val updatedUser = user.updateDisplayName("New Display Name")

        updatedUser.displayName shouldBe "New Display Name"
        updatedUser.updatedAt shouldNotBe user.updatedAt
    }

    @Test
    fun `should update bio successfully`() {
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

        val updatedUser = user.updateBio("New bio")

        updatedUser.bio shouldBe "New bio"
        updatedUser.updatedAt shouldNotBe user.updatedAt
    }

    @Test
    fun `should update email successfully`() {
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

        val updatedUser = user.updateEmail("newemail@example.com")

        updatedUser.email shouldBe "newemail@example.com"
        updatedUser.updatedAt shouldNotBe user.updatedAt
    }

    @Test
    fun `should throw exception when updating with invalid email`() {
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

        shouldThrow<IllegalArgumentException> {
            user.updateEmail("invalid-email")
        }
    }

    @Test
    fun `UserId should generate unique values`() {
        val id1 = UserId.generate()
        val id2 = UserId.generate()

        id1 shouldNotBe id2
    }

    @Test
    fun `UserId should create from string`() {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        val userId = UserId.from(uuid)

        userId.value.toString() shouldBe uuid
    }
}
