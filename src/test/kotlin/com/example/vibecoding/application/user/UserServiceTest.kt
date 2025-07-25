package com.example.vibecoding.application.user

import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        userService = UserService(userRepository)
    }

    @Test
    fun `should create user successfully`() {
        val username = "testuser"
        val email = "test@example.com"
        val displayName = "Test User"
        val bio = "Test bio"

        every { userRepository.existsByUsername(username) } returns false
        every { userRepository.existsByEmail(email) } returns false
        every { userRepository.save(any()) } answers { firstArg() }

        val createdUser = userService.createUser(username, email, displayName, bio)

        createdUser.username shouldBe username
        createdUser.email shouldBe email
        createdUser.displayName shouldBe displayName
        createdUser.bio shouldBe bio

        verify { userRepository.existsByUsername(username) }
        verify { userRepository.existsByEmail(email) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating user with existing username`() {
        val username = "testuser"
        val email = "test@example.com"
        val displayName = "Test User"

        every { userRepository.existsByUsername(username) } returns true

        shouldThrow<IllegalArgumentException> {
            userService.createUser(username, email, displayName)
        }

        verify { userRepository.existsByUsername(username) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating user with existing email`() {
        val username = "testuser"
        val email = "test@example.com"
        val displayName = "Test User"

        every { userRepository.existsByUsername(username) } returns false
        every { userRepository.existsByEmail(email) } returns true

        shouldThrow<IllegalArgumentException> {
            userService.createUser(username, email, displayName)
        }

        verify { userRepository.existsByUsername(username) }
        verify { userRepository.existsByEmail(email) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should get user by id successfully`() {
        val userId = UserId.generate()
        val now = LocalDateTime.now()
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        every { userRepository.findById(userId) } returns user

        val foundUser = userService.getUserById(userId)

        foundUser shouldBe user
        verify { userRepository.findById(userId) }
    }

    @Test
    fun `should throw exception when user not found by id`() {
        val userId = UserId.generate()

        every { userRepository.findById(userId) } returns null

        shouldThrow<IllegalArgumentException> {
            userService.getUserById(userId)
        }

        verify { userRepository.findById(userId) }
    }

    @Test
    fun `should get user by username successfully`() {
        val username = "testuser"
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = username,
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        every { userRepository.findByUsername(username) } returns user

        val foundUser = userService.getUserByUsername(username)

        foundUser shouldBe user
        verify { userRepository.findByUsername(username) }
    }

    @Test
    fun `should get user by email successfully`() {
        val email = "test@example.com"
        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = "testuser",
            email = email,
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        every { userRepository.findByEmail(email) } returns user

        val foundUser = userService.getUserByEmail(email)

        foundUser shouldBe user
        verify { userRepository.findByEmail(email) }
    }

    @Test
    fun `should get all users successfully`() {
        val now = LocalDateTime.now()
        val users = listOf(
            User(
                id = UserId.generate(),
                username = "user1",
                email = "user1@example.com",
                displayName = "User 1",
                bio = null,
                createdAt = now,
                updatedAt = now
            ),
            User(
                id = UserId.generate(),
                username = "user2",
                email = "user2@example.com",
                displayName = "User 2",
                bio = null,
                createdAt = now,
                updatedAt = now
            )
        )

        every { userRepository.findAll() } returns users

        val foundUsers = userService.getAllUsers()

        foundUsers shouldBe users
        verify { userRepository.findAll() }
    }

    @Test
    fun `should update user display name successfully`() {
        val userId = UserId.generate()
        val now = LocalDateTime.now()
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )
        val newDisplayName = "Updated Name"

        every { userRepository.findById(userId) } returns user
        every { userRepository.save(any()) } answers { firstArg() }

        val updatedUser = userService.updateUserDisplayName(userId, newDisplayName)

        updatedUser.displayName shouldBe newDisplayName
        verify { userRepository.findById(userId) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `should update user email successfully`() {
        val userId = UserId.generate()
        val now = LocalDateTime.now()
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )
        val newEmail = "newemail@example.com"

        every { userRepository.findById(userId) } returns user
        every { userRepository.findByEmail(newEmail) } returns null
        every { userRepository.save(any()) } answers { firstArg() }

        val updatedUser = userService.updateUserEmail(userId, newEmail)

        updatedUser.email shouldBe newEmail
        verify { userRepository.findById(userId) }
        verify { userRepository.findByEmail(newEmail) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `should throw exception when updating email to existing one`() {
        val userId = UserId.generate()
        val otherUserId = UserId.generate()
        val now = LocalDateTime.now()
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )
        val otherUser = User(
            id = otherUserId,
            username = "otheruser",
            email = "other@example.com",
            displayName = "Other User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )
        val newEmail = "other@example.com"

        every { userRepository.findById(userId) } returns user
        every { userRepository.findByEmail(newEmail) } returns otherUser

        shouldThrow<IllegalArgumentException> {
            userService.updateUserEmail(userId, newEmail)
        }

        verify { userRepository.findById(userId) }
        verify { userRepository.findByEmail(newEmail) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should delete user successfully`() {
        val userId = UserId.generate()
        val now = LocalDateTime.now()
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        every { userRepository.findById(userId) } returns user
        every { userRepository.deleteById(userId) } returns true

        val result = userService.deleteUser(userId)

        result shouldBe true
        verify { userRepository.findById(userId) }
        verify { userRepository.deleteById(userId) }
    }

    @Test
    fun `should throw exception when deleting non-existent user`() {
        val userId = UserId.generate()

        every { userRepository.findById(userId) } returns null

        shouldThrow<IllegalArgumentException> {
            userService.deleteUser(userId)
        }

        verify { userRepository.findById(userId) }
        verify(exactly = 0) { userRepository.deleteById(userId) }
    }

    @Test
    fun `should check username availability`() {
        val username = "testuser"

        every { userRepository.existsByUsername(username) } returns false

        val isAvailable = userService.isUsernameAvailable(username)

        isAvailable shouldBe true
        verify { userRepository.existsByUsername(username) }
    }

    @Test
    fun `should check email availability`() {
        val email = "test@example.com"

        every { userRepository.existsByEmail(email) } returns true

        val isAvailable = userService.isEmailAvailable(email)

        isAvailable shouldBe false
        verify { userRepository.existsByEmail(email) }
    }
}
