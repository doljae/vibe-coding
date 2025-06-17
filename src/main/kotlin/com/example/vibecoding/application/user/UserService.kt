package com.example.vibecoding.application.user

import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Interface for User service operations
 */
interface UserService {
    fun createUser(username: String, email: String, displayName: String): User
    fun getUserById(userId: UserId): User
    fun getUserByUsername(username: String): User
    fun updateUser(userId: UserId, displayName: String, bio: String?): User
    fun deleteUser(userId: UserId)
    fun userExists(userId: UserId): Boolean
    fun usernameExists(username: String): Boolean
}

/**
 * Implementation of UserService
 */
@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override fun createUser(username: String, email: String, displayName: String): User {
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("Username '$username' already exists")
        }
        
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email '$email' already exists")
        }

        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = username,
            email = email,
            displayName = displayName,
            bio = null,
            createdAt = now,
            updatedAt = now
        )

        return userRepository.save(user)
    }

    override fun getUserById(userId: UserId): User {
        return userRepository.findById(userId)
            ?: throw IllegalArgumentException("User with id '$userId' not found")
    }

    override fun getUserByUsername(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User with username '$username' not found")
    }

    override fun updateUser(userId: UserId, displayName: String, bio: String?): User {
        val user = getUserById(userId)
        val updatedUser = user.copy(
            displayName = displayName,
            bio = bio,
            updatedAt = LocalDateTime.now()
        )
        return userRepository.save(updatedUser)
    }

    override fun deleteUser(userId: UserId) {
        if (userRepository.findById(userId) == null) {
            throw IllegalArgumentException("User with id '$userId' not found")
        }
        userRepository.deleteById(userId)
    }

    override fun userExists(userId: UserId): Boolean {
        return userRepository.findById(userId) != null
    }

    override fun usernameExists(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }

    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User with email '$email' not found")
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    fun updateUserDisplayName(id: UserId, newDisplayName: String): User {
        return updateUser(id, newDisplayName, getUserById(id).bio)
    }

    fun updateUserBio(id: UserId, newBio: String?): User {
        return updateUser(id, getUserById(id).displayName, newBio)
    }

    fun updateUserEmail(id: UserId, newEmail: String): User {
        val user = getUserById(id)
        
        // Check if the new email is already taken by another user
        val existingUser = userRepository.findByEmail(newEmail)
        if (existingUser != null && existingUser.id != id) {
            throw IllegalArgumentException("Email '$newEmail' is already taken")
        }
        
        val updatedUser = user.copy(
            email = newEmail,
            updatedAt = LocalDateTime.now()
        )
        return userRepository.save(updatedUser)
    }

    fun isUsernameAvailable(username: String): Boolean {
        return !userRepository.existsByUsername(username)
    }

    fun isEmailAvailable(email: String): Boolean {
        return !userRepository.existsByEmail(email)
    }
}

