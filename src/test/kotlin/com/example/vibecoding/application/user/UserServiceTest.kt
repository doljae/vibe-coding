package com.example.vibecoding.application.user

import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class UserServiceTest {

    private lateinit var userRepository: FakeUserRepository
    private lateinit var userService: UserServiceImpl

    @BeforeEach
    fun setUp() {
        userRepository = FakeUserRepository()
        userService = UserServiceImpl(userRepository)
    }

    @Test
    fun `should create user successfully`() {
        // Given
        val username = "testuser"
        val email = "test@example.com"
        val displayName = "Test User"

        // When
        val createdUser = userService.createUser(username, email, displayName)

        // Then
        assertEquals(username, createdUser.username)
        assertEquals(email, createdUser.email)
        assertEquals(displayName, createdUser.displayName)
        assertNull(createdUser.bio)

        // Verify the user was saved
        val savedUser = userRepository.findById(createdUser.id)
        assertNotNull(savedUser)
    }

    @Test
    fun `should throw exception when creating user with existing username`() {
        // Given
        val username = "testuser"
        val email = "test@example.com"
        val displayName = "Test User"
        
        // Create a user with the same username
        userService.createUser(username, "other@example.com", "Other User")

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            userService.createUser(username, email, displayName)
        }
    }

    @Test
    fun `should throw exception when creating user with existing email`() {
        // Given
        val username = "testuser"
        val email = "test@example.com"
        val displayName = "Test User"
        
        // Create a user with the same email
        userService.createUser("otheruser", email, "Other User")

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            userService.createUser(username, email, displayName)
        }
    }

    @Test
    fun `should get user by id successfully`() {
        // Given
        val user = userService.createUser("testuser", "test@example.com", "Test User")

        // When
        val foundUser = userService.getUserById(user.id)

        // Then
        assertEquals(user, foundUser)
    }

    @Test
    fun `should throw exception when user not found by id`() {
        // Given
        val nonExistentId = UserId.generate()

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            userService.getUserById(nonExistentId)
        }
    }

    @Test
    fun `should get user by username successfully`() {
        // Given
        val username = "testuser"
        val user = userService.createUser(username, "test@example.com", "Test User")

        // When
        val foundUser = userService.getUserByUsername(username)

        // Then
        assertEquals(user, foundUser)
    }

    @Test
    fun `should throw exception when user not found by username`() {
        // Given
        val nonExistentUsername = "nonexistent"

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            userService.getUserByUsername(nonExistentUsername)
        }
    }

    @Test
    fun `should update user successfully`() {
        // Given
        val user = userService.createUser("testuser", "test@example.com", "Test User")
        val newDisplayName = "Updated Name"
        val newBio = "Updated Bio"

        // When
        val updatedUser = userService.updateUser(user.id, newDisplayName, newBio)

        // Then
        assertEquals(newDisplayName, updatedUser.displayName)
        assertEquals(newBio, updatedUser.bio)
        assertEquals(user.id, updatedUser.id)
        assertEquals(user.username, updatedUser.username)
        assertEquals(user.email, updatedUser.email)
    }

    @Test
    fun `should delete user successfully`() {
        // Given
        val user = userService.createUser("testuser", "test@example.com", "Test User")

        // When
        userService.deleteUser(user.id)

        // Then
        assertFalse(userService.userExists(user.id))
    }

    @Test
    fun `should throw exception when deleting non-existent user`() {
        // Given
        val nonExistentId = UserId.generate()

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            userService.deleteUser(nonExistentId)
        }
    }

    @Test
    fun `should check if username exists`() {
        // Given
        val username = "testuser"
        userService.createUser(username, "test@example.com", "Test User")

        // When
        val exists = userService.usernameExists(username)

        // Then
        assertTrue(exists)
    }

    @Test
    fun `should return false when username does not exist`() {
        // Given
        val nonExistentUsername = "nonexistent"

        // When
        val exists = userService.usernameExists(nonExistentUsername)

        // Then
        assertFalse(exists)
    }

    // Fake repository implementation for testing
    class FakeUserRepository : UserRepository {
        private val users = mutableMapOf<UserId, User>()
        
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
    }
}

